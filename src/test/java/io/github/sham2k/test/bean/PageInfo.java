package io.github.sham2k.test.bean;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PageInfo
{
    @NotNull
    @Min(0)
    @Max(10000)
    private int pageNo;

    @NotNull
    @Min(0)
    @Max(100)
    private int pageSize;

    @Length(min = 0, max = 16)
    private String orderBy;
}
