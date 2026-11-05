package mkle.zeroperdite.services;

import java.util.Optional;
import mkle.zeroperdite.data.User;
import mkle.zeroperdite.data.UserRepository;
import mkle.zeroperdite.data.dto.UserDto;
import mkle.zeroperdite.utils.ZeroPerditeException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository,  PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public Optional<User> get(String id) {
        return repository.findById(id);
    }

    @Transactional
    public User insertUser(User user) {
        if(user.getId() != null && repository.findById(user.getId()).isPresent() || findUserByUsername(user.getUsername()).isEmpty()){
            throw new ZeroPerditeException("User already exists, cannot create new user with given id or username.");
        }
        User saved = repository.save(user);
//        try {
//            FileUtils.forceMkdir(new File(String.format(StaticResources.PRIVATE_FILESYSTEM_DIRECTORY, saved.getId())));
//            FileUtils.forceMkdir(new File(String.format(StaticResources.PRIVATE_NOTES_DIRECTORY, saved.getId())));
//        } catch (IOException e) {
//            FileUtils.deleteQuietly(new File(StaticResources.FILESYSTEM_DIRECTORY + File.separator + saved.getId()));
//            throw new CloudIoException("Error during user base directory creation", e);
//        }
        return saved;
    }

    @Transactional
    public User updateUser(User user) {
        if(user.getId() != null && repository.findById(user.getId()).isEmpty()){
            throw new ZeroPerditeException("Cannot update user value, there is no user with provided id.");
        }
        // Questo metodo salva un'entita' ricostruita dal DTO, quindi ogni campo assente nel
        // payload verrebbe azzerato: i campi qui sotto vengono invece conservati se non passati.
        if(user.getHashedPassword() == null){
            User oldUser = repository.findById(user.getId()).get();
            if(user.getHashedPassword() == null){
                user.setHashedPassword(oldUser.getHashedPassword());
            }
//            if(user.getPermissions() == null){
//                user.setPermissions(new LinkedHashSet<>(oldUser.getPermissions()));
//            }
//            if(user.getDailyKcalTarget() == null){
//                user.setDailyKcalTarget(oldUser.getDailyKcalTarget());
//            }
        }
        return repository.save(user);
    }

    @Transactional
    public Optional<User> findUserByUsername(String username) {
        return repository.findByUsername(username);
    }


//    public void delete(String id) {
//        repository.deleteById(id);
//    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public User convertToUser(UserDto userDto) {
        User user = userDto.toUser();
        if(userDto.password() != null) user.setHashedPassword(encoder.encode(userDto.password()));
        return user;
    }

}
