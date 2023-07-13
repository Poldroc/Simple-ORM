package dao;

import com.wp.annotation.Column;
import lombok.Data;

@Data
public class User {

    Integer id;
    String name;
    String password;
    @Column("phone")
    String phoneNumber;
    Integer isDelete;
}
