package me.cbhud.TrackRig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.cbhud.TrackRig.model.AppUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Health", description = "Simple connectivity test endpoint")
@RestController
@RequestMapping("/api")
public class SimpleController {

    @Operation(summary = "Test authenticated access", description = "Returns the authenticated user's name and role.")
    @ApiResponse(responseCode = "200", description = "Returns greeting with user name and role")
    @GetMapping("/test")
    public String helloWorld(@AuthenticationPrincipal AppUser user){
        String name = user.getFullName();
        String role = user.getRole().toString();
        return "Hello, " + name + "\nYou are: " + role;
    }
}
