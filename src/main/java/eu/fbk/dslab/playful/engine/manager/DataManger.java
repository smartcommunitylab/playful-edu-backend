package eu.fbk.dslab.playful.engine.manager;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import eu.fbk.dslab.playful.engine.model.Learner;
import eu.fbk.dslab.playful.engine.model.LearningScenario;
import eu.fbk.dslab.playful.engine.repository.LearnerRepository;
import eu.fbk.dslab.playful.engine.repository.LearningScenarioRepository;

@Service
public class DataManger {
	private static transient final Logger logger = LoggerFactory.getLogger(DataManger.class);
	
	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	LearningScenarioRepository learningScenarioRepository;
	@Autowired
	LearnerRepository learnerRepository;
	
	public Page<Learner> getLearnerScenario(String domainId, String learningScenarioId, 
			String text, Pageable pageRequest) {
		Criteria criteria = new Criteria("domainId").is(domainId);
		
        if(StringUtils.isNotBlank(learningScenarioId)) {
        	LearningScenario scenario = learningScenarioRepository.findById(learningScenarioId).orElse(null);
        	if(scenario != null) {
        		criteria = criteria.and("id").in(scenario.getLearners());
        	}
        }
        
        if(StringUtils.isNotBlank(text)) {
        	criteria = criteria.orOperator(Criteria.where("email").regex(text, "i"), Criteria.where("nickname").regex(text, "i"),
        			Criteria.where("firstname").regex(text, "i"), Criteria.where("lastname").regex(text, "i"));
        }
        
        Query query = new Query(criteria);
        List<Learner> result = mongoTemplate.find(query, Learner.class);
		return new PageImpl<>(result, pageRequest, result.size());
	}
}