package tyrant.mikera.tyrant;

import java.util.HashMap;
import java.util.Map;

import tyrant.mikera.engine.Thing;



public class RogueLikeFilter implements IThingFilter {
    private Map types = new HashMap();
    
    public RogueLikeFilter() {
        setupMappings();
    }

    private void setupMappings() {
        types.put("%", "IsFood");
        types.put("$", "IsMoney");
        types.put("]", "IsEquipment");
        types.put("[", "IsArmour");
        types.put("=", "IsRing");
        types.put("?", "IsScroll");
        types.put("!", "IsPotion");
        types.put("+", "IsBook");
        types.put("(", "IsWeapon");
        types.put(")", "IsRangedWeapon");
        types.put("/", "IsMissile");
    }

    public boolean accept(Thing thing, String text) {
        if(text == null) return true;
        text = text.trim();
        if(text.length() == 0) return true;
        String type = (String) types.get(text);
        return type != null && thing.get(type) != null;
    }
    
    public boolean isRougeMatch(String aChar) {
        return types.containsKey(aChar);
    }
}
