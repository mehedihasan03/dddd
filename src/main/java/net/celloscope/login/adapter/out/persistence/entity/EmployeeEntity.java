package net.celloscope.login.adapter.out.persistence.entity;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "employee")
public class EmployeeEntity {

    @Id
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
