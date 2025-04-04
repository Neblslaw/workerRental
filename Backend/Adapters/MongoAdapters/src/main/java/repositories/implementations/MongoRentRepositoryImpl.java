package repositories.implementations;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import documents.RentMgd;
import documents.WorkerMgd;
import exceptions.RentAlreadyEndedException;
import exceptions.ResourceNotFoundException;
import exceptions.WorkerRentedException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;
import mongoConnection.CredentialsMongoConnection;
import repositories.interfaces.MongoRentRepository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public class MongoRentRepositoryImpl extends BaseMongoRepository<RentMgd> implements MongoRentRepository {
    public MongoRentRepositoryImpl(CredentialsMongoConnection mongoConnection) {
        super(mongoConnection, RentMgd.class);
    }

    @Override
    protected void createCollection() {
        super.createCollection();
    }

    private List<RentMgd> findByFilter(Bson filter) {
        return getCollection().find(filter).into(new ArrayList<>());
    }

    @Override
    public List<RentMgd> findByUser_IdAndEndDateBefore(UUID id, LocalDateTime date) {
        Document idFilter = new Document("user._id",id);
        //endDate < targetDate
        Document endDateFilter = new Document("endDate",new Document("$lt", date));
        return findByFilter(Filters.and(idFilter,endDateFilter));
    }

    @Override
    public List<RentMgd> findByUser_IdAndEndDateIsNull(UUID id) {
        Document idFilter = new Document("user._id",id);
        Document endDateFilter = new Document("endDate", new Document("$in", Arrays.asList(null, "")));
        return findByFilter(Filters.and(idFilter,endDateFilter));
    }

    @Override
    public List<RentMgd> findByWorker_IdAndEndDateBefore(UUID id, LocalDateTime date) {
        Document idFilter = new Document("worker._id",id);
        //endDate < targetDate
        Document endDateFilter = new Document("endDate",new Document("$lt", date));
        return findByFilter(Filters.and(idFilter,endDateFilter));
    }

    @Override
    public List<RentMgd> findByWorker_IdAndEndDateIsNull(UUID id) {
        Document idFilter = new Document("worker._id",id);
        Document endDateFilter = new Document("endDate", new Document("$in", Arrays.asList(null, "")));
        return findByFilter(Filters.and(idFilter,endDateFilter));
    }

    @Override
    public boolean existsByWorker_IdAndEndDateIsNull(UUID id) {
        Document idFilter = new Document("worker._id",id);
        Document endDateFilter = new Document("endDate", new Document("$in", Arrays.asList(null, "")));
        return getCollection().countDocuments(Filters.and(idFilter,endDateFilter)) > 0;
    }

    @Override
    public List<RentMgd> findAll() {
        return mongoFindAll();
    }

    @Override
    public RentMgd findById(UUID id) {
        return mongoFindById(id);
    }

    private void updateIsRented(WorkerMgd workerMgd, int value){
        MongoCollection<WorkerMgd> workerMongoCollection = mongoConnection.getMongoDatabase().getCollection(WorkerMgd.class.getSimpleName(), WorkerMgd.class);
        Bson filter = Filters.eq("_id", workerMgd.getId());
        Bson update = Updates.inc("isRented", value);
        workerMongoCollection.updateOne(filter, update);
    }

    @Override
    public RentMgd save(RentMgd rent) throws ResourceNotFoundException, RentAlreadyEndedException, WorkerRentedException {
        if(rent.getId() == null){
            rent.setId(UUID.randomUUID());
            return insert(rent);
        }
        RentMgd rentFromDB = mongoFindById(rent.getId());
        if(rentFromDB == null){
            throw new ResourceNotFoundException();
        }
        if(rentFromDB.getEndDate() != null){
            throw new RentAlreadyEndedException();
        }
        return update(rent);
    }

    private RentMgd insert(RentMgd rent) throws WorkerRentedException {
        rent.getUser().removePassword();
        try{
            inSession(mongoConnection.getMongoClient(),()->{
                updateIsRented(rent.getWorker(), 1);
                getCollection().insertOne(rent);
            });
            return rent;
        }catch (MongoWriteException e){
            throw new WorkerRentedException();
        }
    }

    private RentMgd update(RentMgd rent){
        if(rent.getEndDate() != null){
            return finish(rent);
        }
        rent.getUser().removePassword();
        getCollection().replaceOne(new Document("_id", rent.getId()),rent);
        return rent;
    }

    private RentMgd finish(RentMgd rent){
        rent.getUser().removePassword();
        inSession(mongoConnection.getMongoClient(),()->{
            updateIsRented(rent.getWorker(), -1);
            getCollection().replaceOne(new Document("_id", rent.getId()),rent);
        });
        return rent;
    }

    @Override
    public void delete(RentMgd rent) {
        mongoDelete(rent);
    }

    @Override
    public void deleteById(UUID t) {
        mongoDeleteById(t);
    }

    @Override
    public void deleteAll() {
        mongoDeleteAll();
    }
}
