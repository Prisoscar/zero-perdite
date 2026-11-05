package mkle.zeroperdite.io;

import mkle.zeroperdite.data.User;
import mkle.zeroperdite.data.dto.UserDto;
import mkle.zeroperdite.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/insert")
    public ResponseEntity<User> add(@RequestBody UserDto userDto){
        User user = userService.convertToUser(userDto);
        User inserted = userService.insertUser(user);
        return ResponseEntity.ok(inserted);
    }

    @PostMapping("/update")
    public ResponseEntity<User> update(@RequestBody UserDto userDto){
        User user = userService.convertToUser(userDto);
        User updated = userService.updateUser(user);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<User>> list(@RequestAttribute("pageNumber") Integer pageNumber, @RequestAttribute("pageSize") Integer pageSize) throws InterruptedException {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(userService.list(pageRequest));
    }
}
