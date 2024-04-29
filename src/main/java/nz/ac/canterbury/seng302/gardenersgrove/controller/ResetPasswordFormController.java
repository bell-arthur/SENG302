package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Gardener;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailUserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenerFormService;
import nz.ac.canterbury.seng302.gardenersgrove.service.InputValidationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ResetPasswordFormController {
    Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private final GardenerFormService gardenerFormService;
    private final TokenService tokenService;

    private Gardener gardener;

    @Autowired
    public ResetPasswordFormController(GardenerFormService gardenerFormService,
                                       TokenService tokenService) {
        this.gardenerFormService = gardenerFormService;
        this.tokenService = tokenService;
    }

    /**
     * Displays the form for resetting the password
     *
     * @param token Unique token from reset link, used to get the gardener
     * @return The reset password form template
     */
    @GetMapping("/resetPassword")
    public String getResetPasswordForm(@RequestParam(name = "token") String token) {
        logger.info("GET /resetPassword");
        // Verifies token has associated user and is not expired
        String result = tokenService.validateLostPasswordToken(token);
        if (result == null) {
            Optional<Gardener> tempGardener = tokenService.findGardenerbyToken(token);
            if (tempGardener.isPresent()) {
                gardener = tempGardener.get();
                return  "resetPasswordForm";
            };
            return "redirect:/login"; // Gardener / Id not present
        }
        return "redirect:/login?expired"; // Token does not exist or is expired
    }

    /**
     * Validates and process the submitted reset password form
     * @param password The new password
     * @param retypePassword The re-entered new password
     * @param model (map-like) representation of error messages for use in thymeleaf
     * @return
     */
    @PostMapping("/resetPassword")
    public String sendResetPasswordForm(@RequestParam(name = "password") String password,
                                        @RequestParam(name = "retypePassword") String retypePassword,
                                        Model model) {
        logger.info("POST /resetPassword");
        InputValidationService inputValidator = new InputValidationService(gardenerFormService);
        Optional<String> passwordMatchError = inputValidator.checkPasswordsMatch(password, retypePassword);
        model.addAttribute("passwordsMatch", passwordMatchError.orElse(""));
        Optional<String> passwordStrengthError = inputValidator.checkStrongPassword(password);
        model.addAttribute("passwordStrong", passwordStrengthError.orElse(""));

        if (passwordMatchError.isEmpty() && passwordStrengthError.isEmpty()) {
            gardener.updatePassword(password);
            gardenerFormService.addGardener(gardener);

            // Send email telling user password has been updated
            String email = gardener.getEmail();
            String emailMessage = "Your Password has been updated";
            email = "benmoore1.work@gmail.com"; // TESTING
            EmailUserService emailService = new EmailUserService(email, emailMessage);
            emailService.sendEmail(); // Sending email is SLOW

            // Re-authenticates user to catch case when they change their email
//            Authentication newAuth = new UsernamePasswordAuthenticationToken(gardener.getEmail(), gardener.getPassword(), gardener.getAuthorities());
//            SecurityContextHolder.getContext().setAuthentication((newAuth)); // do i need this part for resetting?
            return "redirect:/login";
        }
        return "resetPasswordForm";
    }
}
