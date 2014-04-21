/*
 * TBString.java
 *
 * Status (set the status below e.g.: done, error description, etc.)
 * DocMerge:
 * DocVerified: done
 * Compiles: code reviewed 12-Jul-2013 Helmut
 * Tested:
 */
package com.webobjects.appserver._private;

import java.text.Format;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

/**
 * Displays a string of arbitrary length in the target web page.
 *
 * @binding value
 * @binding dateFormat
 * @binding numberFormat
 * @binding formatter
 * @binding valueWhenEmpty
 * @binding escapeHTML
 *
 */
public class TBString extends WODynamicElement {

    private WOAssociation _value;

    private WOAssociation _dateFormat;
    private WOAssociation _numberFormat;
    private WOAssociation _formatter;

    private boolean _shouldFormat;

    private WOAssociation _escapeHTML;

    private WOAssociation _valueWhenEmpty;

    public TBString(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
        super(null, null, null);

        System.out.println("TBString:: constructor");

        _value = associations.objectForKey(WOHTMLAttribute.Value);

        if (_value == null) {
            throw new WODynamicElementCreationException("<" + this.getClass().getName() + "> ( no 'value' attribute specified.");
        }

        _valueWhenEmpty = associations.objectForKey(WOHTMLAttribute.ValueWhenEmpty);

        _escapeHTML = associations.objectForKey(WOHTMLAttribute.EscapeHTML);
        _dateFormat = associations.objectForKey(WOHTMLAttribute.DateFormat);
        _numberFormat = associations.objectForKey(WOHTMLAttribute.NumberFormat);
        _formatter = associations.objectForKey(WOHTMLAttribute.Formatter);

        _shouldFormat = (_dateFormat != null || _numberFormat != null || _formatter != null);

        int formattersCount = ((_formatter != null) ? 1 : 0) + ((_dateFormat != null) ? 1 : 0) + ((_numberFormat != null) ? 1 : 0);

        if (formattersCount > 1) {
            throw new WODynamicElementCreationException("<" + this.getClass().getName() + "> "+
                          "( the 'dateFormat' and 'numberFormat' or 'formatter' attributes are mutually exclusive, set at most one of these.");
        }
    }

    @Override
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {

        System.out.println("TBString:: appendToResponse:");

        String valueToAppend = null;
        WOComponent aComponent = aContext.component();
        Object valueValue = null;
  
        if (_value != null) {

            valueValue = _value.valueInComponent(aComponent);

            if (_shouldFormat) {

                Format aFormatter = WOFormatterRepository.formatterForInstance(valueValue, aComponent, _dateFormat, _numberFormat, _formatter);

                if (aFormatter != null) {
                    try {
                        valueValue = aFormatter.format(valueValue);
                    } catch (IllegalArgumentException ex) {
                        System.err.println(ex.toString());
                        valueValue = null;
                    }
                }
            }

        } else {
            System.err.println("<" + this.getClass().getName() + " | appendToResponse> WARNING value binding is null !");
        }

        if (valueValue != null) {
            valueToAppend = "xxx"+valueValue.toString()+"yyy";
        }

        if ((valueToAppend == null || valueToAppend.length() == 0) && _valueWhenEmpty != null) {

            valueToAppend = (String) _valueWhenEmpty.valueInComponent(aComponent);
            aResponse.appendContentString(valueToAppend);
  
        } else if (valueToAppend != null) {

            boolean shouldEscapeHTML = true;

            if (_escapeHTML != null) {
                shouldEscapeHTML = _escapeHTML.booleanValueInComponent(aComponent);
            }

            if (shouldEscapeHTML) {
                aResponse.appendContentHTMLString(valueToAppend);
            } else {
                aResponse.appendContentString(valueToAppend);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<" + this.getClass().getName());
        sb.append(" dateFormat=" + _dateFormat);
        sb.append(" numberFormat=" + _numberFormat);
        sb.append(" formatter=" + _formatter);
        sb.append(" value=" + _value);
        sb.append(" escapeHTML=" + _escapeHTML);
        sb.append(" valueWhenEmpty=" + _valueWhenEmpty);
        sb.append(" shouldFormat=" + _shouldFormat);
        sb.append(">");
        return sb.toString();
    }
}
