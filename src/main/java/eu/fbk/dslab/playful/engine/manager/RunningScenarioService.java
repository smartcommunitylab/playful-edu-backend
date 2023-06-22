package eu.fbk.dslab.playful.engine.manager;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import eu.fbk.dslab.playful.engine.dto.ActivityDto;
import eu.fbk.dslab.playful.engine.dto.ComposedActivityDto;
import eu.fbk.dslab.playful.engine.dto.ComposedActivityRunDto;
import eu.fbk.dslab.playful.engine.dto.LearningFragmentDto;
import eu.fbk.dslab.playful.engine.dto.LearningModuleDto;
import eu.fbk.dslab.playful.engine.dto.LearningScenarioDto;
import eu.fbk.dslab.playful.engine.model.Activity;
import eu.fbk.dslab.playful.engine.model.Activity.Type;
import eu.fbk.dslab.playful.engine.model.ActivityStatus;
import eu.fbk.dslab.playful.engine.model.ActivityStatus.Status;
import eu.fbk.dslab.playful.engine.model.ComposedActivity;
import eu.fbk.dslab.playful.engine.model.ComposedActivityRun;
import eu.fbk.dslab.playful.engine.model.Concept;
import eu.fbk.dslab.playful.engine.model.Educator;
import eu.fbk.dslab.playful.engine.model.ExternalActivity;
import eu.fbk.dslab.playful.engine.model.Learner;
import eu.fbk.dslab.playful.engine.model.LearningFragment;
import eu.fbk.dslab.playful.engine.model.LearningFragmentRun;
import eu.fbk.dslab.playful.engine.model.LearningModule;
import eu.fbk.dslab.playful.engine.model.LearningModuleRun;
import eu.fbk.dslab.playful.engine.model.LearningScenario;
import eu.fbk.dslab.playful.engine.model.LearningScenarioRun;
import eu.fbk.dslab.playful.engine.repository.ActivityRepository;
import eu.fbk.dslab.playful.engine.repository.ActivityStatusRepository;
import eu.fbk.dslab.playful.engine.repository.ComposedActivityRepository;
import eu.fbk.dslab.playful.engine.repository.ConceptRepository;
import eu.fbk.dslab.playful.engine.repository.EducatorRepository;
import eu.fbk.dslab.playful.engine.repository.ExternalActivityRepository;
import eu.fbk.dslab.playful.engine.repository.LearnerRepository;
import eu.fbk.dslab.playful.engine.repository.LearningFragmentRepository;
import eu.fbk.dslab.playful.engine.repository.LearningModuleRepository;
import eu.fbk.dslab.playful.engine.repository.LearningScenarioRepository;
import eu.fbk.dslab.playful.engine.repository.LearningScenarioRunRepository;

@Service
public class RunningScenarioService {
	private static transient final Logger logger = LoggerFactory.getLogger(RunningScenarioService.class);
	
	@Autowired
	LearningScenarioRepository learningScenarioRepository;
	
	@Autowired
	LearningModuleRepository learningModuleRepository;
	
	@Autowired
	LearningFragmentRepository learningFragmentRepository;
	
	@Autowired
	ComposedActivityRepository composedActivityRepository;
	
	@Autowired
	ActivityRepository activityRepository;
	
	@Autowired
	ActivityStatusRepository activityStatusRepository;
	
	@Autowired
	LearningScenarioRunRepository learningScenarioRunRepository;
	
	@Autowired
	LearnerRepository learnerRepository;
	
	@Autowired
	EducatorRepository educatorRepository;
	
	@Autowired
	ExternalActivityRepository externalActivityRepository;
	
	@Autowired
	ConceptRepository conceptRepository;
	
	public LearningScenarioRun getLearningScenarioRun(String learningScenarioId, String learnerId) throws HttpClientErrorException {
		LearningScenarioRun scenarioRun = learningScenarioRunRepository.findByLearningScenarioIdAndLearnerId(learningScenarioId, learnerId);
		if(scenarioRun != null) {
			return scenarioRun;
		}
		throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
	}
	
	public List<ActivityStatus> getActivityStatus(List<String> ids) {
		return activityStatusRepository.findByIdIn(ids);
	}
	
	public void runLearningScenario(String learningScenarioId) throws HttpClientErrorException {
		LearningScenario learningScenario = learningScenarioRepository.findById(learningScenarioId).orElse(null);
		if(learningScenario != null) {
			
			List<Learner> learners = learnerRepository.findByIdIn(learningScenario.getLearners());
			
			for(Learner learner : learners) {
				LearningScenarioRun scenarioRun = new LearningScenarioRun();
				scenarioRun.setDomainId(learningScenario.getDomainId());
				scenarioRun.setLearningScenarioId(learningScenarioId);
				scenarioRun.setStartingDate(new Date());
				scenarioRun.setLearnerId(learner.getId());
				learningScenarioRunRepository.save(scenarioRun);
				
				List<LearningModule> modules = learningModuleRepository.findByLearningScenarioId(learningScenarioId);
				for(LearningModule module : modules) {
					LearningModuleRun moduleRun = new LearningModuleRun();
					moduleRun.setLearningModuleId(module.getId());
					scenarioRun.getModules().add(moduleRun);
					
					LearningFragment fragment = learningFragmentRepository.findFirstByLearningModuleId(module.getId());
					if(fragment != null) {
						LearningFragmentRun fragmentRun = new LearningFragmentRun();
						fragmentRun.setLearningFragmentId(fragment.getId());
						moduleRun.setFragment(fragmentRun);
						
						List<ComposedActivity> composedActivities = composedActivityRepository.findByLearningFragmentId(fragment.getId());
						for(ComposedActivity composedActivity : composedActivities) {
							ComposedActivityRun composedActivityRun = new ComposedActivityRun();
							composedActivityRun.setComposedActivityId(composedActivity.getId());
							composedActivityRun.setType(composedActivity.getType());
							fragmentRun.getComposedActivities().add(composedActivityRun);
							
							List<Activity> activities = activityRepository.findByComposedActivityId(composedActivity.getId());
							for(Activity activity : activities) {
								if(activity.getType().equals(Type.concrete)) {
									ActivityStatus activityStatus = new ActivityStatus();
									activityStatus.setDomainId(learningScenario.getDomainId());
									activityStatus.setActivityId(activity.getId());
									activityStatus.setExternalActivityId(activity.getExternalActivityId());
									activityStatus.setComposedActivityId(composedActivity.getId());
									activityStatus.setLearningFragmentId(fragment.getId());
									activityStatus.setLearningModuleId(module.getId());
									activityStatus.setLearningScenarioId(learningScenarioId);
									activityStatus.setLearningScenarioRunId(scenarioRun.getId());
									activityStatus.setLearnerId(learner.getId());
									activityStatus.setLastUpdate(new Date());
									activityStatusRepository.save(activityStatus);
									composedActivityRun.getActivityStatusIds().add(activityStatus.getId());
								} else {
									logger.warn(String.format("skip abstract activity[%s]:%s", activity.getId(), activity.getTitle()));
								}
							}
						}
					}
				}
				learningScenarioRunRepository.save(scenarioRun);
			}
		}
	}
	
	public ComposedActivityRunDto getNextActivity(String learningScenarioId, String learnerId) throws HttpClientErrorException {
		LearningScenarioRun scenarioRun = learningScenarioRunRepository.findByLearningScenarioIdAndLearnerId(learningScenarioId, learnerId);
		if(scenarioRun != null) {
			for(LearningModuleRun moduleRun : scenarioRun.getModules()) {
				for(ComposedActivityRun activityRun : moduleRun.getFragment().getComposedActivities()) {
					ComposedActivityRunDto activityRunDto = new ComposedActivityRunDto(activityRun);					
					List<ActivityStatus> list = activityStatusRepository.findByIdIn(activityRun.getActivityStatusIds());
					boolean found = false;
					for(ActivityStatus activityStatus : list) {
						if(activityStatus.getStatus().equals(Status.assigned)) {
							found = true;
							activityRunDto.getActivities().add(activityStatus);
						}
					}
					if(found) {
						return activityRunDto;
					}
				}
			}
		}
		throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
	}
	
	public void changeActivityStatus(String activityStatusId, String status) throws HttpClientErrorException {
		ActivityStatus activityStatus = activityStatusRepository.findById(activityStatusId).orElse(null);
		if(activityStatus != null) {
			activityStatus.setStatus(Status.valueOf(status));
			activityStatusRepository.save(activityStatus);
		} else {
			throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
		}
	}

	public LearningScenarioDto getLearningScenario(String learningScenarioId) throws HttpClientErrorException {
		LearningScenario learningScenario = learningScenarioRepository.findById(learningScenarioId).orElse(null);
		if(learningScenario != null) {
			LearningScenarioDto scenarioDto = new LearningScenarioDto(learningScenario);
			
			List<Learner> learners = learnerRepository.findByIdIn(learningScenario.getLearners());
			scenarioDto.getLearners().addAll(learners);
			
			List<Educator> educators = educatorRepository.findByIdIn(learningScenario.getEducators());
			scenarioDto.getEducators().addAll(educators);
			
			List<LearningModule> modules = learningModuleRepository.findByLearningScenarioId(learningScenarioId);
			for(LearningModule module : modules) {
				LearningModuleDto moduleDto = new LearningModuleDto(module);
				scenarioDto.getModules().add(moduleDto);
				
				LearningFragment fragment = learningFragmentRepository.findFirstByLearningModuleId(module.getId());
				LearningFragmentDto fragmentDto = new LearningFragmentDto(fragment);
				moduleDto.setFragment(fragmentDto);
				
				List<ComposedActivity> composedActivities = composedActivityRepository.findByLearningFragmentId(fragment.getId());
				for(ComposedActivity composedActivity : composedActivities) {
					ComposedActivityDto composedActivityDto = new ComposedActivityDto(composedActivity);
					fragmentDto.getComposedActivities().add(composedActivityDto);
					
					List<Activity> activities = activityRepository.findByComposedActivityId(composedActivity.getId());
					for(Activity activity : activities) {
						ActivityDto activityDto = new ActivityDto(activity);
						composedActivityDto.getActivities().add(activityDto);
						
						ExternalActivity externalActivity = externalActivityRepository.findById(activity.getExternalActivityId()).orElse(null);
						activityDto.setExternalActivity(externalActivity);
						
						List<Concept> goals = conceptRepository.findByIdIn(activity.getGoals());
						activityDto.getGoals().addAll(goals);
					}
				}
			}
			return scenarioDto;
		} else {
			throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
		}
	}
	
}