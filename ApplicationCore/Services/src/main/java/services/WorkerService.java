package services;

import Entities.WorkerEnt;
import exceptions.ResourceNotFoundException;
import exceptions.WorkerRentedException;
import infrastructure.RentRepository;
import infrastructure.WorkerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class WorkerService {

    private WorkerRepository workerRepository;
    private RentRepository rentRepository;

    public WorkerEnt findById(UUID id) throws ResourceNotFoundException {
        if(workerRepository.findById(id).isPresent()){
            return workerRepository.findById(id).get();
        }
        else
            throw new ResourceNotFoundException();
    }

    public List<WorkerEnt> findAll() {
        return workerRepository.findAll();
    }

    public WorkerEnt save(WorkerEnt worker) {
        return workerRepository.save(worker);
    }

    public WorkerEnt updateWorker(WorkerEnt worker) throws ResourceNotFoundException {
        if (workerRepository.findById(worker.getId()).isEmpty()){
            throw new ResourceNotFoundException("Worker with id " + worker.getId() + " not found");
        }
        return workerRepository.save(worker);
    }

    public void delete(UUID id) throws ResourceNotFoundException, WorkerRentedException {
        if(workerRepository.findById(id).isEmpty()){
            throw new ResourceNotFoundException();
        }
        if(rentRepository.existsByWorker_IdAndEndDateIsNull(id)){
            throw new WorkerRentedException();
        }
       workerRepository.deleteById(id);
    }
}
