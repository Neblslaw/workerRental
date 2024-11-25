package indie.outsource.mvc;


import indie.outsource.user.CreateUserDTO;
import indie.outsource.user.USERTYPE;
import indie.outsource.user.UserDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Controller
public class UserController {

    private final WebClient webClient;

    @Autowired
    public UserController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        CreateUserDTO createUserDTO = new CreateUserDTO();
        createUserDTO.setType(USERTYPE.CLIENT);
        model.addAttribute("user", createUserDTO);

        return "registerForm";
    }

    @PostMapping("/registerSubmit")
    public String registerSubmit(@ModelAttribute("user") @Valid CreateUserDTO createUserDTO, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("user", createUserDTO);
            return "registerForm";
        }

        UserDTO user = webClient.post().uri("/users").bodyValue(createUserDTO).retrieve().onStatus(
                HttpStatusCode::is4xxClientError,
                response -> response.bodyToMono(String.class)
                        .map(errorBody -> new RuntimeException("4xx Error: " + errorBody))
                )
                .bodyToMono(UserDTO.class).onErrorResume(
                        _ ->{
                            bindingResult.addError(new ObjectError("user", "Username already exists"));
                            return Mono.empty();
                        }
                )
                .block();
        if(bindingResult.hasErrors()) {
            model.addAttribute("user", createUserDTO);
            return "registerForm";
        }

        model.addAttribute("user", user);
        return "account";
    }
}
