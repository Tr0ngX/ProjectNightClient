package net.minecraft.client.renderer.rendertype;

public final class RenderTypeHelper {
    private RenderTypeHelper() {}

    public static RenderType create(String name, RenderSetup setup) {
        return RenderType.create(name, setup);
    }
}
