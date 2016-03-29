package hello;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Treated as Controller and ResponseBody. Handles web requests
@RestController
public class HelloController {

	// Handles any requests to the root by responding with plain text
	@RequestMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

}