/**
 * Komprimiert ein Bild (als Uint8Array) mit der angegebenen Qualität (0.0–1.0)
 * und gibt ein Promise<Uint8Array> mit den komprimierten JPEG-Daten zurück.
 */
async function compressImageBytes(bytes, quality) {
  // 1) Eingabe-Bytes in ein Blob packen
  const inputBlob = new Blob([bytes], { type: "image/jpeg" });
  // Wenn deine Eingabe eher PNG ist, ggf. "image/png" setzen.

  // 2) Blob in ein Image-Objekt laden
  const url = URL.createObjectURL(inputBlob);
  try {
    const img = await loadImage(url);

    // 3) Canvas in Bildgröße erzeugen
    const canvas = document.createElement("canvas");
    canvas.width = img.naturalWidth || img.width;
    canvas.height = img.naturalHeight || img.height;

    const ctx = canvas.getContext("2d");
    if (!ctx) {
      throw new Error("2D canvas context not available");
    }

    // 4) Bild auf Canvas zeichnen
    ctx.drawImage(img, 0, 0);

    // 5) Canvas als JPEG-Blob mit Qualität exportieren
    const outputBlob = await canvasToJpegBlob(canvas, quality);

    // 6) Blob wieder in Uint8Array konvertieren
    const arrayBuffer = await outputBlob.arrayBuffer();
    return new Uint8Array(arrayBuffer);
  } finally {
    URL.revokeObjectURL(url);
  }
}

/**
 * Lädt ein Image aus einer URL und gibt ein HTMLImageElement zurück.
 */
function loadImage(url) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve(img);
    img.onerror = (e) => reject(new Error("Image load failed: " + e));
    img.src = url;
  });
}

/**
 * Wandelt ein Canvas in einen JPEG-Blob mit gegebener Qualität um.
 */
function canvasToJpegBlob(canvas, quality) {
  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => {
        if (blob) resolve(blob);
        else reject(new Error("canvas.toBlob returned null"));
      },
      "image/jpeg",
      quality
    );
  });
}