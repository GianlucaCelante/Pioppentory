package it.pioppi.business.dto.settings;

public abstract class BaseSettingDto {

    private String name;
    private String description;
    private SettingType type;
    private int icon;

    public BaseSettingDto(String name, String description, SettingType type, int icon) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SettingType getType() {
        return type;
    }

    public void setType(SettingType type) {
        this.type = type;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
