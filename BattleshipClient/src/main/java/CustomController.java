public interface CustomController {
    // Called after the scene is accessible
    public void postInit();
    public void updateUI(GUICommand command);

    public void onResizeWidth(Number oldVal, Number newVal);

    public void onResizeHeight(Number oldVal, Number newVal);

    public void onRenderUpdate(double deltaTime);
}
