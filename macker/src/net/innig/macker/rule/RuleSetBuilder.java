package net.innig.macker.rule;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;

public class RuleSetBuilder
    {
    public RuleSetBuilder()
        { saxBuilder = new SAXBuilder(false); }
        
    public Collection/*<RuleSet>*/ build(InputStream is)
        throws JDOMException, RulesException
        { return build(saxBuilder.build(is)); }

    public Collection/*<RuleSet>*/ build(Reader reader)
        throws JDOMException, RulesException
        { return build(saxBuilder.build(reader)); }

    public Collection/*<RuleSet>*/ build(File file)
        throws JDOMException, RulesException
        { return build(saxBuilder.build(file)); }

    public Collection/*<RuleSet>*/ build(String fileName)
        throws JDOMException, RulesException
        { return build(saxBuilder.build(fileName)); }

    public Collection/*<RuleSet>*/ build(Document doc)
        throws RulesException
        {
        Collection ruleSets = new ArrayList();
        for(Iterator rsIter = doc.getRootElement().getChildren("ruleset").iterator(); rsIter.hasNext(); )
            ruleSets.add(buildRuleSet((Element) rsIter.next(), null));
        return ruleSets;
        }

    public RuleSet buildRuleSet(Element ruleSetElem, RuleSet parent)
        throws RulesException
        {
        RuleSet ruleSet = new RuleSet(parent);
        
        String name = ruleSetElem.getAttributeValue("name");
        if(name != null)
            ruleSet.setName(name);
        
        for(Iterator patIter = ruleSetElem.getChildren().iterator(); patIter.hasNext(); )
            {
            Element subElem = (Element) patIter.next();
            String subElemName = subElem.getName();
            if(subElemName.equals("pattern"))
                {
                String patternName = subElem.getAttributeValue("name");
                if(ruleSet.declaresPattern(patternName))
                    throw new RulesDocumentException(
                        subElem,
                        "Pattern named \"" + patternName + "\" is already defined in this context");

                ruleSet.setPattern(patternName, buildPattern(subElem, ruleSet, true));
                }
            else if(subElemName.equals("access-rule"))
                ruleSet.addRule(buildAccessRule(subElem, ruleSet));
            else if(subElemName.equals("foreach"))
                ruleSet.addRule(buildForEach(subElem, ruleSet));
            else if(subElemName.equals("ruleset"))
                ruleSet.addRule(buildRuleSet(subElem, ruleSet));
            }
        
        return ruleSet;
        }

    public Pattern buildPattern(
            Element patternElem,
            RuleSet ruleSet,
            boolean isTopElem)
        throws RulesException
        {
        CompositePatternType patType;
        if(patternElem.getName().equals("include"))
            patType = CompositePatternType.INCLUDE;
        else if(patternElem.getName().equals("exclude"))
            patType = CompositePatternType.EXCLUDE;
        else if(isTopElem)
            patType = CompositePatternType.INCLUDE;
        else
            throw new RulesDocumentException(
                patternElem,
                "Invalid element <" + patternElem.getName() + "> --"
                + " expected <include> or <exclude>");
        
        String otherPatName = patternElem.getAttributeValue("pattern");
        String regex = patternElem.getAttributeValue("regex");
        if(!isTopElem && (otherPatName == null) == (regex == null))
            throw new RulesDocumentException(
                patternElem,
                "<include> and <exclude> tags must have either a \"pattern\" "
                + " or a \"regex\" attribute, but cannot have both");
        Pattern head = null;
        if(regex != null)
            head = new RegexPattern(regex);
        else if(otherPatName != null)
            {
            head = ruleSet.getPattern(otherPatName);
            if(head == null)
                throw new UndeclaredPatternException(otherPatName);
            }
        
        CompositePattern firstChildPat = null, prevChildPat = null;        
        for(Iterator childIter = patternElem.getChildren().iterator(); childIter.hasNext(); )
            {
            Element subElem = (Element) childIter.next();
            if(subElem.getName().equals("message"))
                continue;
                            
            CompositePattern pat = forceCompositePattern(buildPattern(subElem, ruleSet, false));
            
            if(firstChildPat == null)
                firstChildPat = pat;
            else
                prevChildPat.setNext(pat);
            prevChildPat = pat;
            }
        
        if(firstChildPat == null && head == null)
            throw new RulesDocumentException(
                patternElem,
                '<' + patternElem.getName() + "> element must have "
                + "a \"regex\" or \"pattern\" attribute, or "
                + "contain at least one <include> or <exclude>");
        
        if(head == null)
            return firstChildPat;
        else if(firstChildPat == null && patType == CompositePatternType.INCLUDE)
            return head;
        else
            return new CompositePattern(patType, head, firstChildPat);
        }
    
    private CompositePattern forceCompositePattern(Pattern pat)
        {
        return (pat instanceof CompositePattern)
            ? (CompositePattern) pat
            : new CompositePattern(CompositePatternType.INCLUDE, pat);
        }
    
    public ForEach buildForEach(Element forEachElem, RuleSet parent)
        throws RulesException
        {
        String varName = forEachElem.getAttributeValue("var");
        if(varName == null)
            throw new RulesDocumentException(
                forEachElem,
                "<foreach> is missing the \"var\" attribute");
        
        String regex = forEachElem.getAttributeValue("regex");
        if(regex == null)
            throw new RulesDocumentException(
                forEachElem,
                "<foreach> is missing the \"regex\" attribute");
        
        ForEach forEach = new ForEach();
        forEach.setVariableName(varName);
        forEach.setRegex(regex);
        forEach.setRuleSet(buildRuleSet(forEachElem, parent));
        return forEach;
        }
    
    public AccessRule buildAccessRule(Element ruleElem, RuleSet ruleSet)
        throws RulesException
        {
        AccessRule prevRule = null, topRule = null;
        for(Iterator childIter = ruleElem.getChildren().iterator(); childIter.hasNext(); )
            {
            Element subElem = (Element) childIter.next();
            AccessRule accRule = new AccessRule();
            
            if(subElem.getName().equals("allow"))
                accRule.setType(AccessRuleType.ALLOW);
            else if(subElem.getName().equals("deny"))
                accRule.setType(AccessRuleType.DENY);
            else if(subElem.getName().equals("from")
                 || subElem.getName().equals("to"))
                continue;
            else
                throw new RulesDocumentException(
                    subElem,
                    "Invalid element <" + subElem.getName() + "> --"
                    + " expected a accerence rule (<deny> or <allow>)");
            
            Element fromElem = subElem.getChild("from");
            if(fromElem != null)
                {
                accRule.setFromMessage(fromElem.getChildText("message"));
                accRule.setFrom(buildPattern(fromElem, ruleSet, true));
                }
            
            Element toElem = subElem.getChild("to");
            if(toElem != null)
                {
                accRule.setToMessage(toElem.getChildText("message"));
                accRule.setTo(buildPattern(toElem, ruleSet, true));
                }

            if(!subElem.getChildren().isEmpty())
                accRule.setChild(buildAccessRule(subElem, ruleSet));
            
            if(topRule == null)
                topRule = accRule;
            else
                prevRule.setNext(accRule);
            prevRule = accRule;
            }
        return topRule;
        }
    
    private SAXBuilder saxBuilder;
    }



