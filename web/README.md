# Matrix Rain Visualizer

This project implements the classic "Digital Rain" effect from the Matrix movie series using HTML5 Canvas and vanilla JavaScript.

## Features

- Classic green falling characters effect.
- Uses a mix of Katakana, Latin characters, and numbers.
- Responsive design that adjusts to window resizing.
- Lightweight with no external dependencies.

## How to Run

You can open the `index.html` file directly in your web browser, or serve it using the included Python script.

### Using Python

1.  Make sure you have Python installed.
2.  Run the server script:

    ```bash
    python serve.py
    ```

3.  Open your web browser and navigate to:

    [http://localhost:8000](http://localhost:8000)

### Custom Port

You can specify a custom port as an argument:

```bash
python serve.py 9090
```

Then access it at `http://localhost:9090`.

## Customization

You can tweak the effect by editing `index.html`:
- **Color**: Change `ctx.fillStyle = '#0F0'` to any hex or RGB color.
- **Speed**: Adjust the `setInterval` delay (currently `33`ms).
- **Font Size**: Change the `fontSize` constant.