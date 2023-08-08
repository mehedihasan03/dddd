package net.celloscope.login.domain;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedisTempData {

    private String userOid;
    private String username;
    private String token;
    private String userStatus;
    private String userRoleOid;
    private String roleOid;
    private String roleName;
    private String roleStatus;
    private String employeeOid;
    private String fullName;
    private String designation;
    private String mobileNumber;
    private String email;
    private String employeeStatus;
    private String branchOid;
    private String branchCode;
    private String branchName;
    private String branchStatus;
    private String mfiOid;
    private String mfiName;
    private String mfiLicenseNumber;
    private String mfiStatus;

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
    }
}
