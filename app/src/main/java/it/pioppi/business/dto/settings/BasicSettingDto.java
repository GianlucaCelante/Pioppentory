package it.pioppi.business.dto.settings;

public class BasicSettingDto<T> extends BaseSettingDto {

    private T value;

    public BasicSettingDto(String name, String description, SettingType type, int icon, T value) {
        super(name, description, type, icon);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

}