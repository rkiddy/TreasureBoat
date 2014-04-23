/*
Disclaimer of Warranties. Author(s) disclaim(s) to the fullest extent authorized by law any and all warranties, whether express or implied,
including, without limitation, any implied warranties of title, non-infringement, enjoyment, integration, merchantability or fitness for any
particular purpose.

Until we decide on a license, see above. -rrk
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
public class WOString extends WODynamicElement {

    private WOAssociation _value;

    private WOAssociation _dateFormat;
    private WOAssociation _numberFormat;
    private WOAssociation _formatter;

    private boolean _shouldFormat;

    private WOAssociation _escapeHTML;

    private WOAssociation _valueWhenEmpty;

    public WOString(String name, NSDictionary<String, WOAssociation> bindings, WOElement template) {
        super(null, null, null);

        System.out.println("WOString:: constructor");

        _value = bindings.objectForKey(WOHTMLAttribute.Value);

        if (_value == null) {
            throw new WODynamicElementCreationException("<" + this.getClass().getName() + "> no 'value' attribute specified.");
        }

        _valueWhenEmpty = bindings.objectForKey(WOHTMLAttribute.ValueWhenEmpty);

        _escapeHTML = bindings.objectForKey(WOHTMLAttribute.EscapeHTML);
        _dateFormat = bindings.objectForKey(WOHTMLAttribute.DateFormat);
        _numberFormat = bindings.objectForKey(WOHTMLAttribute.NumberFormat);
        _formatter = bindings.objectForKey(WOHTMLAttribute.Formatter);

        int formattersCount = ((_formatter != null) ? 1 : 0) + ((_dateFormat != null) ? 1 : 0) + ((_numberFormat != null) ? 1 : 0);

        if (formattersCount > 1) {
            throw new WODynamicElementCreationException("<" + this.getClass().getName() +
                          "> the 'dateFormat' and 'numberFormat' or 'formatter' attributes are mutually exclusive, set at most one of these.");
        }

        _shouldFormat = (_dateFormat != null || _numberFormat != null || _formatter != null);
    }

    @Override
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {

        System.out.println("WOString:: appendToResponse:");

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
            System.err.println("<" + this.getClass().getName() + " | appendToResponse> WARNING value binding is null!");
        }

        if (valueValue != null) {
            valueToAppend = valueValue.toString();
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

        System.out.println("WOString:: toString");

        StringBuilder b = new StringBuilder();
        b.append("<" + this.getClass().getName());
        b.append(" dateFormat=" + _dateFormat);
        b.append(" numberFormat=" + _numberFormat);
        b.append(" formatter=" + _formatter);
        b.append(" value=" + _value);
        b.append(" escapeHTML=" + _escapeHTML);
        b.append(" valueWhenEmpty=" + _valueWhenEmpty);
        b.append(" shouldFormat=" + _shouldFormat);
        b.append(">");
        return b.toString();
    }
}
