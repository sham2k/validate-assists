package io.github.sham2k.test.group;

import io.github.sham2k.test.bean.WebReq1;
import jakarta.validation.groups.Default;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import java.util.ArrayList;
import java.util.List;

public class WebReqGroupProvider implements DefaultGroupSequenceProvider<WebReq1>
{
    @Override public List<Class<?>> getValidationGroups(WebReq1 webReq)
    {
        List<Class<?>> groups = new ArrayList<>();
        groups.add(WebReq1.class);
        groups.add(Default.class);
        if (webReq != null) {
            switch (webReq.getCmdCode()) {
            case "cmd.001":
                groups.add(CREATE.class);
                break;
            case "cmd.002":
                groups.add(DELETE.class);
                break;
            case "cmd.003":
                groups.add(UPDATE.class);
                break;
            case "cmd.004":
                groups.add(SELECT.class);
                break;
            case "cmd.005":
                groups.add(QUERY.class);
                break;
            }
        }
        return groups;
    }
}
