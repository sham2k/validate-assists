package io.github.sham2k.validation.constraints;

import org.hibernate.validator.cfg.ConstraintDef;

public class ValueMapDef extends ConstraintDef<ValueMapDef, ValueMap>
{
    public ValueMapDef()
    {
        super(ValueMap.class);
    }

    public ValueMapDef targetName(String targetName)
    {
        this.addParameter("targetName", targetName);
        return this;
    }

    public ValueMapDef defineName(String defineName)
    {
        this.addParameter("defineName", defineName);
        return this;
    }
}
