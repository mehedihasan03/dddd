package net.celloscope.login.adapter.in.dto.response;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponseDto {

    private String token;
    private String username;
    private String passwordResetRequired;
    private String fullName;
    private String roleName;
    private String email;
    private String mobileNumber;
    private String designation;

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
    }
}
