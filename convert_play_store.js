const sharp = require('sharp');
sharp('icon_play_store.svg')
  .resize(512, 512)
  .ensureAlpha() // يضمن إنه 32-bit (يحتوي على قناة الـ Alpha)
  .png({ compressionLevel: 9 }) // لضمان الحجم أقل من 1 ميجابايت
  .toFile('play_store_icon.png')
  .then(info => console.log('Successfully converted:', info))
  .catch(err => console.error('Error:', err));
