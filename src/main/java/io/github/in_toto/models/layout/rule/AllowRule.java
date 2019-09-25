package io.github.in_toto.models.layout.rule;


public final class AllowRule extends Rule {

    public AllowRule(String pattern) {
        super(pattern);
    }

    @Override
    public RuleType getType() {
        return RuleType.ALLOW;
    }
}
