
package sg;

import java.io.Writer;

import com.sun.xml.xsom.*;
import com.sun.xml.xsom.impl.Const;
import com.sun.xml.xsom.impl.util.SchemaWriter;
import com.sun.xml.xsom.visitor.XSFunction;

public class XSFunctionImpl implements XSFunction<Void> {

    private SchemaWriter _writer;

    public XSFunctionImpl(Writer writer) {
        if (writer == null) throw new IllegalArgumentException("writer is null");
        _writer = new SchemaWriter(writer);
    }

    boolean isAnyType(XSType xstype) {
        String name = xstype.getName();
        String ns = xstype.getTargetNamespace();
        return Const.schemaNamespace.equalsIgnoreCase(ns) &&
                ("anySimpleType".equals(name) || "anyType".equals(name));
    }

    @Override
    public Void empty(XSContentType empty) {
        _writer.empty(empty);
        return null;
    }

    @Override
    public Void particle(XSParticle particle) {
        XSTerm term = particle.getTerm();
        if (term != null) {
            if (term.isWildcard()) {
                term.asWildcard().apply(this);
            }
            else if (term.isModelGroupDecl()) {
                term.asModelGroupDecl().apply(this);
            }
            else if (term.isModelGroup()) {
                term.asModelGroup().apply(this);
            }
            else if (term.isElementDecl()) {
                term.asElementDecl().apply(this);
            }
        }
        return null;
    }

    @Override
    public Void simpleType(XSSimpleType type) {
        // QUICK HACK: don't print the built-in components
        if (type.getTargetNamespace().equals(Const.schemaNamespace))
            return null;

        XSType baseType = type.getBaseType();
        if (!isAnyType(baseType)) {
            // skip xsd:anyType which baseType of things that doesnt have
            // baseType, including itself.
            baseType.apply(this);
        }
        _writer.simpleType(type);
        return null;
    }

    @Override
    public Void elementDecl(XSElementDecl decl) {
        _writer.elementDecl(decl);
        XSType type = decl.getType();
        if (type != null) type.apply(this);
        return null;
    }

    @Override
    public Void modelGroup(XSModelGroup group) {
        for (XSParticle part : group.getChildren()) {
            part.apply(this);
        }
        return null;
    }

    @Override
    public Void modelGroupDecl(XSModelGroupDecl decl) {
        XSModelGroup modelGroup = decl.getModelGroup();
        if (modelGroup != null)
            modelGroup.apply(this);

        return null;
    }

    @Override
    public Void wildcard(XSWildcard wc) {
        _writer.wildcard(wc);
        return null;
    }

    @Override
    public Void annotation(XSAnnotation ann) {
        _writer.annotation(ann);
        return null;
    }

    @Override
    public Void attGroupDecl(XSAttGroupDecl decl) {
        _writer.attGroupDecl(decl);
        return null;
    }

    @Override
    public Void attributeDecl(XSAttributeDecl decl) {
        _writer.attributeDecl(decl);
        return null;
    }

    @Override
    public Void attributeUse(XSAttributeUse use) {
        _writer.attributeUse(use);
        return null;
    }

    @Override
    public Void complexType(XSComplexType type) {
        // QUICK HACK: don't print the built-in components
        if (type.getTargetNamespace().equals(Const.schemaNamespace))
            return null;

        XSType baseType = type.getBaseType();
        if (!isAnyType(baseType)) {
            // skip xsd:anyType which baseType of things that doesnt have
            // baseType, including itself.
            baseType.apply(this);
        }

        _writer.complexType(type);

        XSContentType contentType = type.getContentType();
        if (contentType != null) {
            XSParticle particle = contentType.asParticle();
            if (particle != null) {
                particle.apply(this);
            }
        }

        return null;
    }

    @Override
    public Void facet(XSFacet facet) {
        _writer.facet(facet);
        return null;
    }

    @Override
    public Void identityConstraint(XSIdentityConstraint decl) {
        _writer.identityConstraint(decl);
        return null;
    }

    @Override
    public Void notation(XSNotation notation) {
        _writer.notation(notation);
        return null;
    }

    @Override
    public Void schema(XSSchema schema) {
        _writer.schema(schema);
        return null;
    }

    @Override
    public Void xpath(XSXPath xpath) {
        _writer.xpath(xpath);
        return null;
    }
}
