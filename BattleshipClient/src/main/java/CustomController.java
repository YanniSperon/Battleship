public interface CustomController {
    public void updateUI(GUICommand command);

    public void onResizeWidth(Number oldVal, Number newVal);

    public void onResizeHeight(Number oldVal, Number newVal);
}
