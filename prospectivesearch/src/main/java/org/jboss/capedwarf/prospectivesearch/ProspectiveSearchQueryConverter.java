package org.jboss.capedwarf.prospectivesearch;

import org.jboss.capedwarf.search.CacheValue;
import org.jboss.capedwarf.search.GAEQueryTreeVisitor;
import org.jboss.capedwarf.search.QueryConverter;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
class ProspectiveSearchQueryConverter extends QueryConverter {
    public ProspectiveSearchQueryConverter() {
        super(CacheValue.ALL_FIELD_NAME);
    }

    @Override
    protected GAEQueryTreeVisitor createTreeVisitor(String allFieldName) {
        return new ProspectiveSearchQueryTreeVisitor(allFieldName);
    }
}
