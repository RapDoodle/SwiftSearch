package ink.repo.search.crawler.acl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ACL {
    private List<Pattern> rules;

    public ACL() {
        this.rules = new ArrayList<>();
    }

    public ACL(List<Pattern> rules) {
        this.rules = rules;
    }

    public void addAllowRule(String regex, int regexFlags) {
        this.rules.add(Pattern.compile(regex, regexFlags));
    }

    public void addAllowRule(String regex) {
        this.addAllowRule(regex, 0);
    }

    public void addAllowRules(List<String> rules) {
        this.addAllowRules(rules, 0);
    }

    public void addAllowRules(List<String> rules, int regexFlags) {
        for (String rule : rules)
            this.addAllowRule(rule, regexFlags);
    }

    public void showRules() {
        int n = this.rules.size();
        System.out.println("The access control list has " + n + " rule(s).");
        for (int i = 0; i < n; ++i) {
            System.out.println("  " + (i+1) + ". " + this.rules.get(i).toString());
        }
    }

    public boolean check(String url) {
        if (this.rules.size() == 0)
            return true;
        for (Pattern rule : this.rules)
            if (rule.matcher(url).find())
                return true;
        return false;
    }
}
