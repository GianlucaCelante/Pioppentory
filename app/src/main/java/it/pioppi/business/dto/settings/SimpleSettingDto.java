package it.pioppi.business.dto.settings;

public class SimpleSettingDto<T> extends BaseSettingDto {

    private T value;

    public SimpleSettingDto(String name, String description, int icon, T value) {
        super(name, description, SettingType.BASIC, icon);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

}