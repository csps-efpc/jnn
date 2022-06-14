package com.handshape.justneuralnets.design;

/**
 * @author JoTurner
 */
public class DesignFieldConfig {

    private String fieldName;
    private DataFieldType dataFieldType;
    private Role role;
    private String params;

    public DesignFieldConfig() {
        this.fieldName = "no field";
        this.dataFieldType = DataFieldType.FREE_TEXT;
        this.role = Role.IGNORE;
        this.params = "";
    }

    ;

    public DesignFieldConfig(String fieldName, DataFieldType dataFieldType, Role role, String params) {
        this.fieldName = fieldName;
        this.dataFieldType = dataFieldType;
        this.role = role;
        this.params = params;
    }

    ;

    /**
     * @return the fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * @return the dataFieldType
     */
    public DataFieldType getDataFieldType() {
        return dataFieldType;
    }

    /**
     * @param dataFieldType the dataFieldType to set
     */
    public void setDataFieldType(DataFieldType dataFieldType) {
        this.dataFieldType = dataFieldType;
    }

    /**
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * @return the params
     */
    public String getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(String params) {
        this.params = params;
    }

    public enum DataFieldType {
        BOOLEAN,
        CLOSED_VOCAB,
        CLOSED_VOCAB_MULTIVALUE,
        ENGLISH_WORD_STEM,
        FREE_TEXT,
        NORMALIZED,
        PRENORMALIZED,
        UNBOUNDED,
        ENGLISH_WORD_STEM_INCIDENCE,
        FRENCH_WORD_STEM,
        FRENCH_WORD_STEM_INCIDENCE,
        WORD2VEC_TEXT
    }

    public enum Role {
        IGNORE,
        LABEL,
        DATA
    }

}
