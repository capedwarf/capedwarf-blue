package org.jboss.capedwarf.prospectivesearch;

import org.apache.lucene.search.Query;
import org.jboss.capedwarf.search.Context;
import org.jboss.capedwarf.search.GAEQueryTreeVisitor;
import org.jboss.capedwarf.search.Operator;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
class ProspectiveSearchQueryTreeVisitor extends GAEQueryTreeVisitor {

    public ProspectiveSearchQueryTreeVisitor(String allFieldName) {
        super(allFieldName);
    }

    @Override
    protected Query createNumericQuery(String field, Operator operator, Context value) {
        double doubleValue = Double.parseDouble(value.getText());
        value.setText(DoubleBridge.INSTANCE.objectToString(doubleValue));
        return createTextQuery(field, operator, value);
    }
}
