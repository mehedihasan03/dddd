package net.celloscope.login.domain;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    private String oid;
    private String userOid;
    private String fullName;
    private String designation;
    private String mobileNumber;
    private String email;
    private Timestamp createdOn;
    private String createdBy;
    private Timestamp updatedOn;
    private String updatedBy;
    private String status;
    private Long version;
    private String mfiOid;
    private String branchOid;

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this);
    }

}
