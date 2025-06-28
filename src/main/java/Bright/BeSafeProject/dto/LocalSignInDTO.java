package Bright.BeSafeProject.dto;

public record LocalSignInDTO(
        LocalLogInDTO emailAndPassword,
        String name
) { }
