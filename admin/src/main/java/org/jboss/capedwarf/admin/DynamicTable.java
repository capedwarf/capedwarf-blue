/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.capedwarf.admin;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.html.HtmlColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import java.util.List;

/**
 *
 */
public class DynamicTable extends HtmlPanelGroup {

    private HtmlDataTable table;

    public DynamicTable(String valueExpression) {
        // Create <h:dataTable value="#{datastoreViewer.rows}" var="row">.
        table = new HtmlDataTable();
        table.setValueExpression("value", createValueExpression(valueExpression, List.class));
        table.setWidth("100%");
        table.setVar("row");

        // Add the datatable to <h:panelGroup binding="#{myBean.dynamicDataTableGroup}">.
        getChildren().clear();
        getChildren().add(table);
    }

    public void clearColumns() {
        table.getChildren().clear();
    }

    public void addColumn(String propertyName, String columnHeading) {
        // Create <h:column>.
        HtmlColumn column = new HtmlColumn();
        table.getChildren().add(column);

        // Create <h:outputText value="columnHeading"> for <f:facet name="header"> of column.
        HtmlOutputText header = new HtmlOutputText();
        header.setValue(columnHeading);
        column.setHeader(header);

        // Create <h:outputText value="#{row[" + i + "]}"> for the body of column.
        HtmlOutputText output = new HtmlOutputText();
        output.setValueExpression("value", createValueExpression("#{row." + propertyName + "}", String.class));
        column.getChildren().add(output);
    }

    public void addLinkColumn(String propertyName, String columnHeading, String href) {
        // Create <h:column>.
        HtmlColumn column = new HtmlColumn();
        table.getChildren().add(column);

        // Create <h:outputText value="columnHeading"> for <f:facet name="header"> of column.
        HtmlOutputText header = new HtmlOutputText();
        header.setValue(columnHeading);
        column.setHeader(header);

        HtmlOutputLink link = new HtmlOutputLink();
        link.setValueExpression("value", createValueExpression(href, String.class));
        column.getChildren().add(link);

        HtmlOutputText output = new HtmlOutputText();
        output.setValueExpression("value", createValueExpression("#{row." + propertyName + "}", String.class));
        link.getChildren().add(output);
    }

    private ValueExpression createValueExpression(String valueExpression, Class<?> valueType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExpressionFactory elFactory = facesContext.getApplication().getExpressionFactory();
        return elFactory.createValueExpression(facesContext.getELContext(), valueExpression, valueType);
    }


}
