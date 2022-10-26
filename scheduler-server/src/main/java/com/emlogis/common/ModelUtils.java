package com.emlogis.common;

import com.emlogis.common.security.AccountACE;
import com.emlogis.model.ACE;
import com.emlogis.model.BaseEntity;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.EmployeeSkill;
import com.emlogis.model.employee.Skill;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ModelUtils {

    public static <T extends BaseEntity> T find(Collection<T> entities, String id) {
        if (StringUtils.isNotEmpty(id)) {
            for (T entity : entities) {
                if (id.equals(entity.getId())) {
                    return entity;
                }
            }
        }
        return null;
    }

    public static Set<String> idSet(Collection<? extends BaseEntity> entities) {
        Set<String> result = new HashSet<>();
        if (entities != null) {
            for (BaseEntity entity : entities) {
                result.add(entity.getId());
            }
        }
        return result;
    }

    public static String[] idArray(Collection<? extends BaseEntity> entities) {
        int size = entities.size();
        String[] result = new String[size];
        BaseEntity[] baseEntities = entities.toArray(new BaseEntity[size]);
        for (int i = 0; i < size; i++) {
            result[i] = baseEntities[i].getId();
        }
        return result;
    }

    public static String commaSeparatedQuotedIds(Collection<? extends BaseEntity> entities) {
        return commaSeparatedIds(entities, '\'');
    }

    public static String commaSeparatedIds(Collection<? extends BaseEntity> entities, Character wrapSymbol) {
        return separatedIds(entities, ',', wrapSymbol);
    }

    public static String separatedIds(Collection<? extends BaseEntity> entities, Character separator,
                                      Character wrapSymbol) {
        if (entities == null || entities.size() == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (BaseEntity entity : entities) {
            if (first) {
                first = false;
            } else {
                result.append(separator);
            }
            if (wrapSymbol != null) {
                result.append(wrapSymbol);
            }
            result.append(entity.getId());
            if (wrapSymbol != null) {
                result.append(wrapSymbol);
            }
        }
        return result.toString();
    }

    public static String commaSeparatedQuotedValues(Object[] values) {
        return commaSeparatedValues(values, '\'');
    }

    public static String commaSeparatedValues(Object[] values, Character wrapSymbol) {
        return separatedValues(values, ',', wrapSymbol);
    }

    public static String commaSeparatedQuotedValues(Collection<String> values) {
        return commaSeparatedValues(values, '\'');
    }

    public static String commaSeparatedValues(Collection<String> values, Character wrapSymbol) {
        return separatedValues(values, ',', wrapSymbol);
    }

    public static String separatedValues(Object[] values, char separator, Character wrapSymbol) {
        if (values == null || values.length == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Object value : values) {
            if (first) {
                first = false;
            } else {
                result.append(separator);
            }
            if (wrapSymbol == null) {
                result.append(value);
            } else {
                result.append(wrapSymbol).append(value).append(wrapSymbol);
            }
        }
        return result.toString();
    }

    public static String separatedValues(Collection<String> values, char separator, char wrapSymbol) {
        if (values == null || values.size() == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String value : values) {
            if (first) {
                first = false;
            } else {
                result.append(separator);
            }
            result.append(wrapSymbol).append(value).append(wrapSymbol);
        }
        return result.toString();
    }

    public static AccountACE buildAccountACE(ACE ace) {
        AccountACE result = new AccountACE();
        result.setId(ace.getId());
        result.setEntityClass(ace.getEntityClass());
        result.setPattern(ace.getPattern());
        result.setPermissions(ace.getPermissions());
        result.setDescription(ace.getDescription());
        return result;
    }

    public static Set<AccountACE> buildAccountACESet(Collection<ACE> aces) {
        Set<AccountACE> result = new HashSet<>();
        for (ACE ace : aces) {
            result.add(buildAccountACE(ace));
        }
        return result;
    }

    public static boolean isEmployeeHasSkill(Employee employee, Skill skill) {
        Collection<EmployeeSkill> employeeSkills = employee.getEmployeeSkills();
        if (employeeSkills != null) {
            for (EmployeeSkill employeeSkill : employeeSkills) {
                if (skill.equals(employeeSkill.getSkill())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isEmployeeHasOneOfSkills(Employee employee, Collection<Skill> skills) {
        for (Skill skill : skills) {
            if (isEmployeeHasSkill(employee, skill)) {
                return true;
            }
        }
        return false;
    }

    public static long cutDateTimeToDate(long datetime) {
        return new LocalDate(datetime).toDate().getTime();
    }

    public static long cutDateTimeToDate(long datetime, DateTimeZone timeZone) {
        return new LocalDate(datetime, timeZone).toDate().getTime();
    }

    public static LocalDate cutDateIfLessPredefined(LocalDate date) {
        if (date != null && date.toDate().getTime() < Constants.DATE_2000_01_01) {
            return new LocalDate(0);
        } else {
            return date;
        }
    }

    public static DateTime cutDateIfLessPredefined(DateTime date) {
        if (date != null && date.toDate().getTime() < Constants.DATE_2000_01_01) {
            return new DateTime(0);
        } else {
            return date;
        }
    }

    public static <T extends Enum> String commaSeparatedEnumOrdinals(Collection<T> values) {
        String result = null;
        if (values != null && values.size() > 0) {
            for (T value : values) {
                if (StringUtils.isEmpty(result)) {
                    result = String.valueOf(value.ordinal());
                } else {
                    result += "," + value.ordinal();
                }
            }
        }
        return result;
    }

}
