package it.pioppi.business.dto.settings;

public class ComplexSettingDto extends BaseSettingDto {

    private Class<?> targetFragment;

    public ComplexSettingDto(String name, String description, int icon, Class<?> targetFragment) {
        super(name, description, SettingType.COMPLEX, icon);
        this.targetFragment = targetFragment;
    }

    public Class<?> getTargetFragment() {
        return targetFragment;
    }

    public void setTargetFragment(Class<?> targetFragment) {
        this.targetFragment = targetFragment;
    }

}