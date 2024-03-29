package eu.fbk.dslab.playful.engine.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import eu.fbk.dslab.playful.engine.model.ExternalActivity;

@Repository
public interface ExternalActivityRepository extends MongoRepository<ExternalActivity, String> {
	public List<ExternalActivity> findByIdIn(List<String> ids);
	public Page<ExternalActivity> findByDomainId(String domainId, Pageable pageRequest);
	public ExternalActivity findOneByDomainIdAndExtId(String domainId, String extId);
	public List<ExternalActivity> findByDomainIdAndGroupCorrelator(String domainId, String groupCorrelator);
	@Query("{'domainId':?0, $or:[{'preconditions':?1}, {'effects':?1}]}")
	public List<ExternalActivity> findByDomainIdAndConceptId(String domainId, String conceptId);
}
