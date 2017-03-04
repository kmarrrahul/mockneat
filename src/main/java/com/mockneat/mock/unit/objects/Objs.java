package com.mockneat.mock.unit.objects;

import com.mockneat.mock.interfaces.MockConstValue;
import com.mockneat.mock.interfaces.MockRandUnitValue;
import com.mockneat.mock.interfaces.MockUnit;
import com.mockneat.mock.interfaces.MockValue;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.mockneat.mock.utils.ValidationUtils.*;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.Validate.*;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.reflect.ConstructorUtils.invokeConstructor;
import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.apache.commons.lang3.reflect.MethodUtils.invokeExactStaticMethod;

public class Objs<T> implements MockUnit<T> {

    private static final Pattern JAVA_FIELD_REGEX =
            compile("^[a-zA-Z_$][a-zA-Z_$0-9]*$");

    private Map<String, MockValue> fields = new LinkedHashMap<>();
    private Class<T> cls;

    private Objs() {}

    public Objs(Class<T> cls) {
        this.cls = cls;
    }

    public Objs type(Class<T> cls) {
        this.cls = cls;
        return this;
    }

    public <T1> Objs<T> field(String fieldName, MockUnit<T1> mockUnit) {
        notNull(mockUnit, INPUT_PARAMETER_NOT_NULL, "mockUnit");
        Validate.notEmpty(fieldName, INPUT_PARAMETER_NOT_NULL_OR_EMPTY, "fieldName");
        isTrue(JAVA_FIELD_REGEX.matcher(fieldName).matches(), JAVA_FIELD_REGEX_MATCH, fieldName);
        this.fields.put(fieldName, new MockRandUnitValue(mockUnit));
        return this;
    }

    public Objs<T> field(String fieldName, Object value) {
        Validate.notEmpty(fieldName, INPUT_PARAMETER_NOT_NULL_OR_EMPTY, "fieldName");
        isTrue(JAVA_FIELD_REGEX.matcher(fieldName).matches(), JAVA_FIELD_REGEX_MATCH, fieldName);
        this.fields.put(fieldName, MockConstValue.val(value));
        return this;
    }

    protected T instance() {
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            String fmt = format(CANNOT_INSTANTIATE_OBJECT_OF_CLASS,
                    cls.getSimpleName(),
                    cls.getSimpleName());
            throw new IllegalArgumentException(fmt, e);
        }
    }

    protected void setValues(T object) {
        fields.forEach((key, val) -> {
            Object cVal = val.get();
            try {
                writeField(object, key, cVal, true);
            } catch (IllegalAccessException e) {
                String fmt = format(CANNOT_SET_FIELD_WITH_VALUE,
                        cls.getSimpleName(),
                        key,
                        cVal);
               throw new IllegalArgumentException(fmt, e);
            }
        });
    }

    protected String listTypes(Object[] objs) {
        final StringBuilder buff = new StringBuilder("(");
        Arrays.stream(objs).forEach(obj -> {
            if (null != obj) { buff.append(obj.getClass().getName()); }
            else { buff.append("null"); }
            buff.append(",");
        });
        buff.deleteCharAt(buff.length()-1);
        buff.append(")");
        return buff.toString();
    }

    public MockUnit<T> constructor(MockUnit... mockUnits) {
        notNull(mockUnits, INPUT_PARAMETER_NOT_NULL, "mockUnits");
        Supplier<T> supp = () -> {
            Object[] args = new Object[mockUnits.length];
            range(0, mockUnits.length).forEach(i -> {
                if (mockUnits[i]==null) { args[i] = null; }
                else { args[i] = mockUnits[i].val(); }
            });
            try {
                return invokeConstructor(cls, args);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                String fmt = format(CANNOT_INFER_CONSTRUCTOR, cls.getName(), listTypes(args));
                throw new IllegalArgumentException(fmt, e);
            }
        };
        return () -> supp;
    }

    //TODO document and test this
    public <T1> MockUnit<T> factory(Class<T1> factory, String method, MockUnit... mockUnits) {
        notNull(factory, INPUT_PARAMETER_NOT_NULL, "factory");
        notEmpty(method, INPUT_PARAMETER_NOT_NULL_OR_EMPTY, "method");
        notNull(mockUnits, INPUT_PARAMETER_NOT_NULL, "mockUnits");
        isTrue(JAVA_FIELD_REGEX.matcher(method).matches(), JAVA_METHOD_REGEX_MATCH, method);
        Supplier<T> supp = () -> {
            Object[] args = new Object[mockUnits.length];
            range(0, mockUnits.length).forEach(i -> {
                if (mockUnits[i]==null) { args[i] = null; }
                else { args[i] = mockUnits[i].val(); }
            });
            try {
                return (T) invokeExactStaticMethod(factory, method, args);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                String fmt = format(CANNOT_INVOKE_STATIC_FACTORY_METHOD,
                                    factory.getClass(),
                                    method,
                                    listTypes(args));
                throw new IllegalArgumentException(fmt, e);
            }
        };
        return () -> supp;
    }


    @Override
    public Supplier<T> supplier() {
        return () -> {
            T instance = instance();
            setValues(instance);
            return instance;
        };
    }
}
