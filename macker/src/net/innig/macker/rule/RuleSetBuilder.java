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
        { saxBuilder = new SAXBuilder(true); }
        
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
                
                CompositePattern pattern = buildCompositePattern(subElem, ruleSet);
                
                if(pattern.getType() == CompositePatternType.EXCLUDE)
                    {
                    CompositePattern includeAll = new CompositePattern();
                    includeAll.setNext(pattern);
                    pattern = includeAll;
                    }
                
                ruleSet.setPattern(patternName, pattern);
                }
            else if(subElemName.equals("access-rule"))
                ruleSet.addRule(buildAccessRule(subElem, ruleSet));
            else if(subElemName.equals("foreach"))
                ruleSet.addRule(buildForEach(subElem, ruleSet));
            }
        
        return ruleSet;
        }

    public CompositePattern buildCompositePattern(Element patternElem, RuleSet ruleSet)
        throws RulesException
        {
        CompositePattern prevPat = null, topPat = null;
        for(Iterator childIter = patternElem.getChildren().iterator(); childIter.hasNext(); )
            {
            Element subElem = (Element) childIter.next();
            
            CompositePatternType patType;
            if(subElem.getName().equals("include"))
                patType = CompositePatternType.INCLUDE;
            else if(subElem.getName().equals("exclude"))
                patType = CompositePatternType.EXCLUDE;
            else if(subElem.getName().equals("message"))
                continue;
            else
                throw new RulesDocumentException(
                    subElem,
                    "Invalid element <" + subElem.getName() + "> --"
                    + " expected <include> or <exclude>");
            
            Pattern head;
            String otherPatName = subElem.getAttributeValue("pattern");
            String regex = subElem.getAttributeValue("regex");
            if((otherPatName == null) == (regex == null))
                throw new RulesDocumentException(
                    subElem,
                    "<include> and <exclude> tags must have either a \"pattern\" or a \"regex\""
                    + " attribute, but not both");
            if(regex != null)
                head = new RegexPattern(regex);
            else // (otherPatName != null)
                {
                head = ruleSet.getPattern(otherPatName);
                if(head == null)
                    throw new UndeclaredPatternException(otherPatName);
                }
            
            CompositePattern pat = new CompositePattern(patType, head);
            
            if(!subElem.getChildren().isEmpty())
                pat.setChild(buildCompositePattern(subElem, ruleSet));
            
            if(topPat == null)
                topPat = pat;
            else
                prevPat.setNext(pat);
            prevPat = pat;
            }
        
        if(topPat == null)
            throw new RulesDocumentException(
                patternElem,
                "Pattern element must contain at least one <include> or <exclude>");
        
        return topPat;
        }
    
    public ForEach buildForEach(Element forEachElem, RuleSet parent)
        throws RulesException
        {
        String name = forEachElem.getAttributeValue("name");
        if(name == null)
            throw new RulesDocumentException(
                forEachElem,
                "<foreach> is missing the \"name\" attribute");
        
        String value = forEachElem.getAttributeValue("value");
        if(value == null)
            throw new RulesDocumentException(
                forEachElem,
                "<foreach> is missing the \"value\" attribute");
        
        ForEach forEach = new ForEach();
        forEach.setName(name);
        forEach.setValue(value);
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
                accRule.setFrom(buildCompositePattern(fromElem, ruleSet));
                }
            
            Element toElem = subElem.getChild("to");
            if(toElem != null)
                {
                accRule.setToMessage(toElem.getChildText("message"));
                accRule.setTo(buildCompositePattern(toElem, ruleSet));
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



