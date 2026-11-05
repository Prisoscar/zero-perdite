package mkle.zeroperdite.services;

import java.util.Optional;
import mkle.zeroperdite.data.Client;
import mkle.zeroperdite.data.ClientRepository;
import mkle.zeroperdite.data.dto.ClientDto;
import mkle.zeroperdite.utils.ZeroPerditeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public Optional<Client> get(String id) {
        return repository.findById(id);
    }

    @Transactional
    public Client insertClient(Client client) {
        if (client.getId() != null && repository.findById(client.getId()).isPresent()) {
            throw new ZeroPerditeException("Client already exists, cannot create new client with given id.");
        }
        return repository.save(client);
    }

    @Transactional
    public Client updateClient(Client client) {
        if (client.getId() != null && repository.findById(client.getId()).isEmpty()) {
            throw new ZeroPerditeException("Cannot update client value, there is no client with provided id.");
        }
        return repository.save(client);
    }

    public Page<Client> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Client> list(Pageable pageable, Specification<Client> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public Client convertToClient(ClientDto clientDto) {
        return clientDto.toClient();
    }
}
