/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */
 
package net.innig.macker.rule;

import net.innig.macker.rule.filter.*;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

import net.innig.util.EnumeratedType;
import net.innig.util.OrderedType;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;

public class RuleSetBuilder
    {
    public RuleSetBuilder()
        { saxBuilder = new SAXBuilder(false); }
        
    public Collection/*<RuleSet>*/ build(InputStream is)
        throws RulesException
        {
        try { return build(saxBuilder.build(is)); }
        catch(JDOMException jdome)
            { throw new RulesDocumentException(jdome); }
        }

    public Collection/*<RuleSet>*/ build(Reader reader)
        throws RulesException
        {
        try { return build(saxBuilder.build(reader)); }
        catch(JDOMException jdome)
            { throw new RulesDocumentException(jdome); }
        }

    public Collection/*<RuleSet>*/ build(File file)
        throws RulesException
        {
        try { return build(saxBuilder.build(file)); }
        catch(JDOMException jdome)
            { throw new RulesDocumentException(jdome); }
        }

    public Collection/*<RuleSet>*/ build(String fileName)
        throws RulesException
        {
        try { return build(saxBuilder.build(fileName)); }
        catch(JDOMException jdome)
            { throw new RulesDocumentException(jdome); }
        }

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
        
        buildSeverity(ruleSet, ruleSetElem);
        
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
            else if(subElemName.equals("subset"))
                {
                if(ruleSet.getSubsetPattern() != null)
                    throw new RulesDocumentException(
                        subElem,
                        "<ruleset> may only contain a single <subset> element");
                ruleSet.setSubsetPattern(buildPattern(subElem, ruleSet, true));
                }
            else if(subElemName.equals("access-rule"))
                ruleSet.addRule(buildAccessRule(subElem, ruleSet));
            else if(subElemName.equals("var"))
                ruleSet.addRule(buildVariable(subElem, ruleSet));
            else if(subElemName.equals("foreach"))
                ruleSet.addRule(buildForEach(subElem, ruleSet));
            else if(subElemName.equals("ruleset"))
                ruleSet.addRule(buildRuleSet(subElem, ruleSet));
            else if(subElemName.equals("message"))
                ruleSet.addRule(buildMessage(subElem, ruleSet));
            }
        
        return ruleSet;
        }

    public Pattern buildPattern(
            Element patternElem,
            RuleSet ruleSet,
            boolean isTopElem)
        throws RulesException
        {
        // handle options
        
        String otherPatName = patternElem.getAttributeValue("pattern");
        String regex = patternElem.getAttributeValue("regex");
        String filterName = patternElem.getAttributeValue("filter");

        CompositePatternType patType;
        if(patternElem.getName().equals("include"))
            patType = CompositePatternType.INCLUDE;
        else if(patternElem.getName().equals("exclude"))
            patType = (filterName == null)
                ? CompositePatternType.EXCLUDE
                : CompositePatternType.INCLUDE;
        else if(isTopElem)
            patType = CompositePatternType.INCLUDE;
        else
            throw new RulesDocumentException(
                patternElem,
                "Invalid element <" + patternElem.getName() + "> --"
                + " expected <include> or <exclude>");
        
        if(otherPatName != null && regex != null)
            throw new RulesDocumentException(
                patternElem,
                "patterns cannot have both a \"pattern\" and a \"regex\" attribute");
        
        // do the head thing
        
        Pattern head = null;
        if(regex != null)
            head = new RegexPattern(regex);
        else if(otherPatName != null)
            {
            head = ruleSet.getPattern(otherPatName);
            if(head == null)
                throw new UndeclaredPatternException(otherPatName);
            }
        
        // build up children
        
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
        
        // pull together composite
        
        Pattern result = null;
        if(head != null || firstChildPat != null)
            {
            if(head == null)
                result = firstChildPat;
            else if(firstChildPat == null && patType == CompositePatternType.INCLUDE)
                result = head;
            else
                result = new CompositePattern(patType, head, firstChildPat);
            }
        
        // wrap it in a filter if necessary
        
        if(filterName != null)
            {
            Map options = new HashMap();
            for(Iterator i = patternElem.getAttributes().iterator(); i.hasNext(); )
                {
                Attribute attr = (Attribute) i.next();
                options.put(attr.getName(), attr.getValue());
                }
            options.remove("name");
            options.remove("pattern");
            options.remove("regex");
            
            Filter filter = FilterFinder.findFilter(filterName);
            result = (result == null)
                ? filter.createPattern(ruleSet, options)
                : filter.createPattern(ruleSet, result, options);
                
            if(patternElem.getName().equals("exclude"))
                result = new CompositePattern(CompositePatternType.EXCLUDE, result, null);
            }
        
        // did we get something out of all that nonsense?
        
        if(result == null)
            throw new RulesDocumentException(
                patternElem,
                '<' + patternElem.getName() + "> element must have"
                + " a \"regex\", \"pattern\", or \"filter\" attribute, or"
                + " contain at least one <include> or <exclude>");
        
        return result;
        }
    
    private CompositePattern forceCompositePattern(Pattern pat)
        {
        return (pat instanceof CompositePattern)
            ? (CompositePattern) pat
            : new CompositePattern(CompositePatternType.INCLUDE, pat);
        }
    
    public Variable buildVariable(Element forEachElem, RuleSet parent)
        throws RulesException
        {
        String varName = forEachElem.getAttributeValue("name");
        if(varName == null)
            throw new RulesDocumentException(
                forEachElem,
                "<var> is missing the \"name\" attribute");
        
        String value = forEachElem.getAttributeValue("value");
        if(value == null)
            throw new RulesDocumentException(
                forEachElem,
                "<var> is missing the \"value\" attribute");
        
        return new Variable(parent, varName, value);
        }
    
    public Message buildMessage(Element messageElem, RuleSet parent)
        throws RulesException
        {
        Message message = new Message(parent, messageElem.getText());
        buildSeverity(message, messageElem);
        return message;
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
        
        ForEach forEach = new ForEach(parent);
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
            AccessRule accRule = new AccessRule(ruleSet);
            
            if(subElem.getName().equals("allow"))
                accRule.setType(AccessRuleType.ALLOW);
            else if(subElem.getName().equals("deny"))
                accRule.setType(AccessRuleType.DENY);
            else if(subElem.getName().equals("from")
                 || subElem.getName().equals("to")
                 || subElem.getName().equals("message"))
                continue;
            else
                throw new RulesDocumentException(
                    subElem,
                    "Invalid element <" + subElem.getName() + "> --"
                    + " expected an access rule (<deny> or <allow>)");
            
            Element fromElem = subElem.getChild("from");
            if(fromElem != null)
                accRule.setFrom(buildPattern(fromElem, ruleSet, true));
            
            Element toElem = subElem.getChild("to");
            if(toElem != null)
                accRule.setTo(buildPattern(toElem, ruleSet, true));

            if(!subElem.getChildren().isEmpty())
                accRule.setChild(buildAccessRule(subElem, ruleSet));
            
            if(topRule == null)
                topRule = accRule;
            else
                prevRule.setNext(accRule);
            prevRule = accRule;
            }
        if(topRule != null)
            {
            topRule.setMessage(ruleElem.getChildText("message"));
            buildSeverity(topRule, ruleElem);
            }
        return topRule;
        }

    public void buildSeverity(Rule rule, Element elem)
        throws RulesDocumentException
        {
        String severityS = elem.getAttributeValue("severity");
        if(severityS != null && !"".equals(severityS))
            {
            RuleSeverity severity;
            try { severity = RuleSeverity.fromName(severityS); }
            catch(IllegalArgumentException iae)
                { throw new RulesDocumentException(elem, iae.getMessage()); }
            rule.setSeverity(severity);
            }
        }
    
    private SAXBuilder saxBuilder;
    }



