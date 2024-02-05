package io.github.sham2k.test.bean;

import io.github.sham2k.test.group.CREATE;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class User
{
    @NotNull
    @Length(min = 3, max = 3)
    private String userCode;

    @NotNull(groups = { CREATE.class})
    private String userName;
}
