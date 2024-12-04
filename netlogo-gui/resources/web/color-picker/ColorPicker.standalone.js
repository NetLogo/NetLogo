(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
  typeof define === 'function' && define.amd ? define(['exports'], factory) :
  (global = typeof globalThis !== 'undefined' ? globalThis : global || self, factory(global.ColorPicker = {}));
})(this, (function (exports) { 'use strict';

  function styleInject(css, ref) {
    if ( ref === void 0 ) ref = {};
    var insertAt = ref.insertAt;

    if (typeof document === 'undefined') { return; }

    var head = document.head || document.getElementsByTagName('head')[0];
    var style = document.createElement('style');
    style.type = 'text/css';

    if (insertAt === 'top') {
      if (head.firstChild) {
        head.insertBefore(style, head.firstChild);
      } else {
        head.appendChild(style);
      }
    } else {
      head.appendChild(style);
    }

    if (style.styleSheet) {
      style.styleSheet.cssText = css;
    } else {
      style.appendChild(document.createTextNode(css));
    }
  }

  var css_248z = "@import url('https://fonts.googleapis.com/css2?family=Inter&display=swap');\n:root {\n  /* gradient-colors: for hue color changing gradient */\n  --gradient-colors: linear-gradient(to right,\n    hsla(0, 100%, 50%, 1),\n    hsla(10, 100%, 50%, 1),\n    hsla(20, 100%, 50%, 1),\n    hsla(30, 100%, 50%, 1),\n    hsla(40, 100%, 50%, 1),\n    hsla(50, 100%, 50%, 1),\n    hsla(60, 100%, 50%, 1),\n    hsla(70, 100%, 50%, 1),\n    hsla(80, 100%, 50%, 1),\n    hsla(90, 100%, 50%, 1),\n    hsla(100, 100%, 50%, 1),\n    hsla(110, 100%, 50%, 1),\n    hsla(120, 100%, 50%, 1),\n    hsla(130, 100%, 50%, 1),\n    hsla(140, 100%, 50%, 1),\n    hsla(150, 100%, 50%, 1),\n    hsla(160, 100%, 50%, 1),\n    hsla(170, 100%, 50%, 1),\n    hsla(180, 100%, 50%, 1),\n    hsla(190, 100%, 50%, 1),\n    hsla(200, 100%, 50%, 1),\n    hsla(210, 100%, 50%, 1),\n    hsla(220, 100%, 50%, 1),\n    hsla(230, 100%, 50%, 1),\n    hsla(240, 100%, 50%, 1),\n    hsla(250, 100%, 50%, 1),\n    hsla(260, 100%, 50%, 1),\n    hsla(270, 100%, 50%, 1),\n    hsla(280, 100%, 50%, 1),\n    hsla(290, 100%, 50%, 1),\n    hsla(300, 100%, 50%, 1),\n    hsla(310, 100%, 50%, 1),\n    hsla(320, 100%, 50%, 1),\n    hsla(330, 100%, 50%, 1),\n    hsla(340, 100%, 50%, 1),\n    hsla(350, 100%, 50%, 1),\n    hsla(360, 100%, 50%, 1)\n  );\n  /* gradietn colors for saturation and brightness */\n  --saturation-gradient: linear-gradient(to right, #fff, #f00);\n  --saturation-thumb: white;\n  --brightness-gradient: linear-gradient(to right, #000, #f00);\n  --brightness-thumb: white;\n}\n\n/* helper classes */\n.cp-invisible {\n    position: absolute;\n    width: 0px;\n    height: 0px;\n    padding: 0;\n    margin: 0;\n    border: none;\n    overflow: hidden;\n}\n\n.cp-no-padding {\n    padding: 0\n}\n\n.no-select {\n  -webkit-touch-callout: none;\n  -webkit-user-select: none; \n  -khtml-user-select: none;\n  -moz-user-select: none; \n  -ms-user-select: none; \n  user-select: none;\n}\n\n/* cp-body: main body, everything in the color picker goes in here */\n.cp {\n  border-radius: 0.625rem;\n  border-top-left-radius: 0.25rem;\n  border-top-right-radius: 0.25rem;\n  background-color: #eeeff0;\n  width: fit-content;\n  height: 27.6rem;\n  box-shadow: 0 0 10px rgb(0 0 0 / 0.2);\n  font-family: 'Lato', sans-serif;\n}\n\n.cp-embedded .cp {\n  width: 100%;\n  height: 100%;\n  box-shadow: none;\n  border-radius: 0;\n}\n\n.cp-header {\n  background-color: #5a648d;\n  width: 100%;\n  height: 2.75rem;\n  border-top-left-radius: 0.25rem;\n  border-top-right-radius: 0.25rem;\n  display: flex;\n  flex-grow: 0;\n  flex-direction: row;\n  justify-content: space-between;\n  align-items: center;\n}\n\n.cp-embedded .cp-header {\n  display: none;\n}\n\n/* cp-title: title of the color picker and icon */\n.cp-title {\n  font-weight: bold;\n  color: white;\n  display: flex;\n  align-items: center;\n  height: 100%;\n  margin-left: 3.1%;\n  gap: 0.75rem;\n}\n/* inverted class for inverting image color for pressing buttons */\n.cp-inverted {\n  filter: invert(100%);\n}\n\n.cp-icon {\n  width: 1.8rem;\n}\n\n.cp-close {\n  width: 1rem;\n  height: 1rem;\n  margin-right: 2.51%;\n  cursor:pointer;\n}\n\n.cp-body {\n  width: 100%;\n  box-sizing: border-box;\n  height: calc(100% - 2.75rem); /* 100% - height of the header */\n  padding: 1.2rem;\n  display: flex;\n  flex-direction: row;\n}\n\n.cp-mode-btn-cont {\n  display: flex;\n  flex-direction: row;\n  width: calc(21rem + 1.5px);\n  justify-content: space-between; \n  padding-bottom: 0.7rem;\n}\n\n.cp-mode-btn {\n  all: unset;\n  display: flex;\n  flex-direction: row;\n  gap: 0.3rem;\n  border: #cecece 1px solid;\n  border-radius: 0.15rem;\n  justify-content: center;\n  align-items: center;\n  height: 1.8rem;\n  width: 4.875rem;\n  box-sizing: border-box;\n  background-color: #e5e5e5;\n  font-size: 0.9rem;\n  cursor: pointer;\n}\n\n.cp-mode-color-display {\n  height: 1.8rem;\n  box-sizing: border-box;\n  width: 4.875rem;\n  border: #cecece 1px solid;\n  visibility: hidden;\n}\n\n.cp-mode-btn-img {\n  width: 0.9rem;\n  aspect-ratio: 1/1;\n}\n\n.cp-model-indicator {\n  width: 100%;\n  margin: 0 0 0.7rem;\n}\n\n/* cp-body-mode: styling for inner body that changes with the mode  */\n.cp-body-mode {\n  display: flex;\n  flex-direction: row;\n}\n/** cp-body-mode-main: the changing portion of the color picker (white section in the middle) */\n.cp-body-mode-main {\n  user-select: none;\n  background-color: white;\n  width: 21rem;\n  height: 20rem;\n  user-select: none;\n  border: solid 1.5px #cecece;\n  border-radius: 0.2rem;\n}\n\n/** cp-body-mode-result: the result portion of the color picker (right section) */\n.cp-body-mode-result {\n  background-color: white;\n}\n\n/** cp-body-mode-left: styling for the left side of the body */\n.cp-body-left {\n  display: flex;\n  flex-direction: column;\n}\n\n/** cp-body-mode-right: styling for the right side of the body */\n.cp-body-right {\n  display: flex;\n  padding-left: 1.5rem;\n  flex-direction: column;\n}\n\n/* When the screen is too small, hide the right part */\n@media (max-width: 37.5rem) {\n  .cp-body-right {\n    display: none;\n  }\n  \n  .cp-mode-color-display {\n    visibility: visible;\n  }\n}\n\n/* additional classes for invisible */ \n.cp-compact .cp-body-right {\n  display: none;\n}\n\n.cp-compact .cp-mode-color-display {\n  visibility: visible;\n}\n\n.cp-color-preview {\n  width: 11rem;\n  height: 11rem;\n  background-color: white;\n  border: solid 1px #cecece;\n  border-radius: 0.2rem;\n}\n\n.cp-alpha-text {\n  font-size: 0.8rem;\n}\n\n.cp-alpha-cont {\n  width: 100%;\n  display: flex;\n  flex-direction: row;\n  justify-content: center;\n  align-items: center;\n  height: 1.83rem;\n  background-color: white;\n  margin-top: 0.53rem;\n  border: solid 1px #C0C0C0;\n  border-radius: 0.2rem;\n  gap: 0.5rem;\n\n}\n\n/* Base style for all sliders */\ninput[type='range'].cp-styled-slider {\n  background: transparent;\n  -webkit-appearance: none;\n  appearance: none;\n  outline: none;\n}\n\n/* Progress support for sliders */\ninput[type='range'].cp-styled-slider.slider-progress {\n  --range: calc(var(--max) - var(--min));\n  --ratio: calc((var(--value) - var(--min)) / var(--range));\n  --sx: calc(0.5 * 2em + var(--ratio) * (100% - 2em));\n}\n\n/* Thumb styles for color and alpha sliders */\ninput[type='range'].cp-styled-slider::-webkit-slider-thumb {\n  -webkit-appearance: none;\n  border-radius: 50%;\n  background: white;\n  box-shadow: 0 0 0.125rem black;\n  cursor: pointer;\n}\n\ninput[type='range'].cp-styled-slider.cp-alpha-slider::-webkit-slider-thumb {\n  width: 1rem;\n  height: 1rem;\n  margin-top: -0.2rem;\n}\n\n/* Unified track style */\ninput[type='range'].cp-styled-slider::-webkit-slider-runnable-track {\n  height: 0.8rem;\n  border: 1px solid #b2b2b2;\n  border-radius: 0.2rem;\n  background: #efefef;\n}\n\n/* Hover effect for all thumbs */\ninput[type='range'].cp-styled-slider::-webkit-slider-thumb:hover {\n  background: white;\n}\n\n/* Specific style for alpha slider */\ninput[type='range'].cp-styled-slider.color-alpha {\n  height: 1rem;\n  width: 7.5rem;\n}\n\n/* Red slider */\ninput[type='range'].cp-styled-slider.color-red.slider-progress::-webkit-slider-runnable-track {\n  background: linear-gradient(#fb4d46, #fb4d46) 0 / var(--sx) 100% no-repeat,\n    #efefef;\n}\n\ninput[type='range'].cp-styled-slider.color-red.slider-progress:hover::-webkit-slider-runnable-track {\n  background: linear-gradient(red, red) 0 / var(--sx) 100% no-repeat, #e5e5e5;\n}\n\n\n/* Alpha slider track color with simplified repeating background */\ninput[type='range'].cp-styled-slider.color-alpha::-webkit-slider-runnable-track{\n  background: linear-gradient(45deg, #ccc 25%, transparent 25%),\n    linear-gradient(-45deg, #ccc 25%, transparent 25%),\n    linear-gradient(45deg, transparent 75%, #ccc 75%),\n    linear-gradient(-45deg, transparent 75%, #ccc 75%),\n    linear-gradient(to right, rgba(0, 0, 0, 0) 0, #ccc 100%);\n  background-size: 0.625rem 0.625rem, 0.625rem 0.625rem, 0.625rem 0.625rem, 0.625rem 0.625rem, 100% 100%;\n  background-position: 0 0, 0 0.313rem, 0.313rem -0.313rem, -0.313rem 0rem, 0 0;\n  background-repeat: repeat, repeat, repeat, repeat, no-repeat;\n}\n\n.cp-color-param-txt {\n  font-weight: bold;\n  font-size: 0.8rem;\n}\n\n/* Green slider */\ninput[type='range'].cp-styled-slider.color-green.slider-progress::-webkit-slider-runnable-track {\n  background: linear-gradient(#50c878, #50c878) 0 / var(--sx) 100% no-repeat,\n    #efefef;\n}\n\ninput[type='range'].cp-styled-slider.color-green.slider-progress:hover::-webkit-slider-runnable-track {\n  background: linear-gradient(green, green) 0 / var(--sx) 100% no-repeat,\n    #e5e5e5;\n}\n\n/* Blue slider */\ninput[type='range'].cp-styled-slider.color-blue.slider-progress::-webkit-slider-runnable-track {\n  background: linear-gradient(#2a52be, #2a52be) 0 / var(--sx) 100% no-repeat,\n    #efefef;\n}\n\ninput[type='range'].cp-styled-slider.color-blue.slider-progress:hover::-webkit-slider-runnable-track {\n  background: linear-gradient(blue, blue) 0 / var(--sx) 100% no-repeat, #e5e5e5;\n}\n\n.cp-values-display {\n  display: flex;\n  flex-direction: row;\n  background-color: white;\n  height: 1.77rem;\n  width: 100%;\n  border: 1px solid #c0c0c0;\n  border-radius: 0.15rem;\n  align-items: center;\n}\n\n.cp-values-cont {\n  display: flex;\n  flex-direction: row;\n  align-items: center;\n}\n\n.cp-values-display-btn {\n  all: unset;\n  cursor: pointer;\n  font-size: 0.8rem;\n  background: #e5e5e5;\n  padding: 0.4rem 0.6rem;\n  border: #cecece 1px solid;\n  border-radius: 0.15rem;\n}\n\n.cp-values-display-btn:hover {\n  background: #f0f0f0; \n}\n\n.cp-values-display-btn:active {\n  background: #e0e0e0; \n  transform: translateY(0);\n  box-shadow: 1px 1px 4px rgba(0, 0, 0, 0.2); \n}\n\n.cp-values-type-text {\n  font-weight: 500;\n  padding-left: 10%;\n}\n\n.cp-values-value-cont {\n  display: flex;\n  width: 65%;\n  justify-content: center;\n}\n/* the first value cont should have a greater width*/\n.cp-values-value-cont-1 {\n  width: 75%;\n}\n\n.cp-values-type {\n  color: #9E9E9E;\n  font-size: 0.8rem;\n  display: flex;\n  flex-direction: row;\n  width: 35%;\n  height: 100%;\n  cursor: pointer;\n}\n/* the first cp-values-type should have a smaller width */\n.cp-values-type-1 {\n  width: 25%;\n}\n\n.cp-values-value {\n  color: #787878;\n  font-size: 0.8rem;\n  cursor: pointer;\n}\n\n.cp-values-img {\n  width: 0.7rem;\n  aspect-ratio: 1/1;\n}\n\n.cp-values-display-cont {\n  margin-top: 0.53rem;\n  display: flex;\n  flex-direction: column;\n  gap: 0.53rem;\n  user-select: none;\n}\n\n/** grid stylings */\n.cp-grid-cont {\n  width: 100%;\n  height: 16.5rem;\n  margin: 0 0 0.7rem 0;\n  overflow: hidden;\n  user-select: none;\n}\n\n.cp-grid-cell {\n  cursor: pointer;\n}\n\n.cp-increment-cont {\n  padding: 0.4rem;\n  display: flex;\n  flex-direction: row;\n  background: #EEEEEE;\n  border-radius: 0.3rem;\n  gap: 0.5rem;\n  justify-content: center;\n  align-items: center;\n}\n\n.cp-grid-btn-cont {\n  display: flex;\n  flex-direction: row;\n  justify-content: center;\n  gap: 0.5rem;\n}\n\n.cp-numbers-btn {\n  all: unset;\n  height: 0.8rem;\n  width: 0.8rem;\n  border-radius: 0.15rem;\n  border: 1px solid #CECECE;\n  cursor: pointer;\n}\n\n.cp-numbers-clicked {\n  background-color: #C0C0C0;\n}\n\n.cp-increment-label {\n  font-size: 0.8rem;\n}\n\n.cp-btn-label-cont {\n  display: flex;\n  flex-direction: row;\n  gap: 0.2rem;\n  justify-content: center;\n  align-items: center;\n}\n\n.cp-grid-text {\n  pointer-events: none;\n}\n\n.cp-wheel-cont {\n  width: 20rem;\n  height: 15rem;\n  overflow: hidden;\n  margin-left: auto;\n  margin-right: auto;\n  border-radius: 0.5rem;\n  user-select: none;\n}\n\n.cp-wheel-spacing-cont {\n  display: flex;\n  flex-direction: column;\n  gap: 1.1rem;\n  padding-top: 1.1rem;\n}\n\n.cp-inner-wheel {\n  width: 100%;\n  height: 100%;\n}\n\n.cp-outer-wheel {\n  width: 100%;\n  height: 100%;\n}\n\n.cp-wheel-numbers {\n  font-size: 0.8rem;\n  color: white;\n}\n\n/** Slider Mode */\n.cp-slider-cont {\n  display: flex;\n  flex-direction: column;\n  justify-content: center;\n  align-items: center;\n  gap: 0.97rem;\n  width: 100%;\n  height: 100%;\n}\n\n.cp-slider-color-display {\n  width: 18rem;\n  height: 4.57rem;\n  box-shadow: 0 0.188rem 0.625rem rgb(0 0 0 / 0.5);\n  border-radius: 0.2rem;\n}\n\n.cp-slider-changer {\n  display: flex;\n  flex-direction: column;\n}\n\n.cp-slider-display-container {\n  display: flex;\n  flex-direction: row;\n  gap: 0.5rem;\n}\n\ninput[type='range'].cp-styled-slider.color-slider::-webkit-slider-thumb {\n  -webkit-appearance: none;\n  width: 1.5rem;\n  height: 1.5rem;\n  border-radius: 50%;\n  background: white;\n  border: none;\n  box-shadow: 0 0 0.125rem black;\n  margin-top: calc(max((1em - 1px - 1px) * 0.5, 0px) - 2em * 0.5);\n  cursor: pointer;\n}\n\ninput[type='number'].cp-slider-value-display-cont {\n  text-align: center;\n  border-radius: 0;\n  border: none;\n  border-bottom: 1px solid #d8d8d8;\n  outline: none;\n  position: relative;\n  margin-top: 1rem;\n  font-size: 0.8rem;\n  padding: 0.125rem 0.125rem 0.25rem;\n  margin-bottom: 0.25rem;\n  width: 2.5rem;\n  background: #fcfcfc;\n}\n\n.cp-slider-text {\n  font-size: 0.9rem;\n  padding-left: 0.3rem;\n  padding-bottom: 0.1rem;\n}\n\n.cp-slider-changer {\n  width: 0.8rem;\n  height: 0.8rem;\n  margin-left: 60%;\n  margin-bottom: -1rem;\n  display: flex;\n  flex-direction: row;\n  justify-content: center;\n  cursor: pointer;\n  user-select: none;\n}\n\n.cp-dropdown-text {\n  color: gray;\n  font-size: 0.8rem;\n}\n\n/** Wheel mode */\n.cp-saved-colors-cont {\n  display: flex;\n  flex-direction: row;\n  gap: 0.8rem;\n}\n\n.cp-saved-colors {\n  background-color: #f1f1f1;\n  box-shadow: 0 0.313rem 0.313rem rgba(0, 0, 0, 0.3);\n  border-radius: 0.2rem;\n  width: 2.8rem;\n  height: 2.8rem;\n  cursor: pointer;\n}\n\n.cp-saved-color-add {\n  background-color: white;\n  border: 2px solid #808080;\n  border-radius: 0.2rem;\n  width: 2.8rem;\n  height: 2.8rem;\n  cursor: pointer;\n  background: linear-gradient(#808080 0 0), linear-gradient(#808080 0 0);\n  background-position: center;\n  background-size: 50% 0.125rem, 0.125rem 50%; \n  background-repeat: no-repeat;\n}\n\n/* Hue slider */\ninput[type='range'].cp-styled-slider.color-hue::-webkit-slider-runnable-track {\n  height: 1em;\n  border: 1px solid #b2b2b2;\n  border-radius: 0.5em;\n  background: var(--gradient-colors);\n  box-shadow: none;\n}\n\ninput[type='range'].cp-styled-slider.color-hue::-webkit-slider-thumb:hover {\n  background: hsl(var(--value), 100%, 60%);\n}\n\ninput[type='range'].cp-styled-slider.color-hue.slider-progress:hover::-webkit-slider-runnable-track {\n  background: var(--gradient-colors);\n}\n\n/* Saturation slider */\ninput[type='range'].cp-styled-slider.color-saturation::-webkit-slider-runnable-track {\n  height: 1em;\n  border: 1px solid #b2b2b2;\n  border-radius: 0.5em;\n  background: var(--saturation-gradient);\n  box-shadow: none;\n}\n\ninput[type='range'].cp-styled-slider.color-saturation::-webkit-slider-thumb:hover {\n  background: var(--saturation-thumb);\n}\n\n/** brightness slider */\ninput[type='range'].cp-styled-slider.color-brightness::-webkit-slider-runnable-track {\n  height: 1em;\n  border: 1px solid #b2b2b2;\n  border-radius: 0.5em;\n  background: var(--brightness-gradient);\n  box-shadow: none;\n}\n\ninput[type='range'].cp-styled-slider.color-brightness::-webkit-slider-thumb:hover {\n  background: var(--brightness-thumb);\n}\n\n.cp-draggable {\n  cursor: pointer;\n}\n\n.cp-wheel-numbers {\n  fill: white;\n  font-family: 'Lato', sans-serif;\n  font-size: 0.25rem;\n  pointer-events: none;\n  user-select: none;\n}";
  styleInject(css_248z);

  var img$6 = "data:image/png;base64,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";

  var img$5 = "data:image/png;base64,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";

  var img$4 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFoAAABaCAYAAAA4qEECAAAACXBIWXMAAG66AABuugHW3rEXAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAOcSURBVHgB7Z07T1RRFIWXKOIjGp8NaCA22ikxIVARSUBM7KSyIbGCX4EJhdJgL52NjaWNCfigUQtj7IzaSKQwGjHxgYKIe2VmyDDO4wwj+27J+pJFQU6x78fM4d5z774H+JsBy23Le8uSZdUhi5Y3lmuWE2icNssNyyvLN8DlGJbzzm5ZBqsVd9Qy41RUtVDMmGU76qfJMm5Zcaq1WmYtHaUF8hevAxRXnAnUz03H+lJCp0eKC5wNVmAhw0hnJKMaa2WmUOBg0AKZj5YDqA0/NXMZ11ot/ZzTLiMuhy09CePOWo4jLlco+jxicy5hTB9i00fRhxCbtoQx7YjNQYregdjsTBjThNg0Ry9wyyDRTki0ExLthEQ7IdFOSLQTEu2ERDsh0U5ItBMS7YREOyHRTki0ExLthEQ7IdFOSLQTEu2ERDsh0U5ItBMS7QRFf0dsUur7gtj8oOh5xOZ5wpiXiM08Rd9FbJ4mjHmE2Nzhj1bEaEWo9BD3NtSmxTKdca2VwlaRtb6c8YAFsvmmC+l0WhYyqLNWxoqL5BQyFag4CruE+hlC7g8U5TjYh1O26WkU2bcocLo4g43They7yz6gpP+m3PzHfhC2KvAp+nb4nGvz9IxnDvyn9sLyE43B4+rO57RlDzYffpPeWR5aHls+QwghhBBCCNEQ5S5Y2OTOE322Bh+zNGPz4ZozL1Se5LOKxuAiEy9Uei0nLfux+fy2vLXctzxD7oUBFeFlIy8fs7x85SpcPYtJpXQi+5U8LmOMlCuOCx8TGRdXHF7ObnRRKdIKHhfq1i1hXA1UXCEU1ol0+C2ItHJXyHihQC5Ke73gqd5wCmhBGhHeB1UuvKnCmyu4HrTAQlLm654AdVbLJOePIcSmN2FMN2JzkaJTXjySJacSxjRyo8CDVorehdjsSxizG7HZ+z88qZRyFzw8eiTMCYl2QqKdkGgnJNoJiXZCop2QaCck2gmJdkKinZBoJyTaCYl2QqKdkGgnJNoJiXZCop2QaCck2gmJdkKinaDoZcRmJWHMEmLzi6IXEJu5hDHRX+7yiaIfIDbTCWOiH8M9/uhH3Mdd+Wlet5VoBdh3k3VLSLWsbfYb9SHuUaQznFGNtbLuNUT81ETb3HcK9ROpD2c177SjtMh2xNjkl2cZ7PvYyKknm57GEKNVhLNE1WnvAnKbiHMzca/mm6/IbZY+iX/zYDz7ctgywk3dFwGXY1jKO+Pm9QOlBf0BajuJFBLntzAAAAAASUVORK5CYII=";

  var img$3 = "data:image/png;base64,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";

  var img$2 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFoAAABaCAYAAAA4qEECAAAACXBIWXMAAG66AABuugHW3rEXAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAARvSURBVHgB7Z1JiFRXFIZ/o5mzSWJcxCRURjDZBCSDhEBGdKEbh53YCioqOCAKogiKKIKoK23FARVRRDe6ckI3rhTnGQVxQpwnHNBuy/P3rSq6mmfVq6p7eqD+Dz5sSqHh99V759577n1Aabqbv5hLzSPmA7PZfGneMfea081vIaribXOQedLMprDJ3GH+CJGa3uZWpAu4rbzSZ5jvQJTkc/MSqgu5tRsQbjsigZ7mOdQect7FEIlsQryQ846EKGIY4odM75ufQqBb7s8L5nfwYYE5D/VFU84CDPpvcx/8eGXeRn3BkC+b6801CGMPLILPbUMGVzJkXtFHzZ8hPBnOoB+bH0F4cpRB8/7xFoQnLxnwKwhvmhn0PQhvdjPo0xCesNSbz6A9a+h655E50TzIh+GX5hX4cYu/CPXFc/OYuda80fovtsOvYG+AKNDPfIb4IXOFRjV6G2YibsgvzN8gElmFOCGzNh8B8Ub4cORsUy0hPzSnQpSFJd9Y8yoqD/k4QnuCqICMudC8ifIBnzBHmx9AJNItxb9hj8ev5p/mNwhLU3zQcZmKi7m7EFZoNGcihBBCCNHhpCnvOhqWk/+Z/yK0BffOfX7NPGPuN3dDK0VV87W5HGGFotyA6QlC/4Qa4itkEsLqRKVTAOyImoIwyBIl4DxLI2qfPdwC9We/EV6FDCgbyUaIRPiVz0Z2EkQRfIhxETd20LzPZyAKsGLIOrkMnYDWdfQf5gTzB7R/fd0HfnPZ7C08hvaF08j8hs5F6NYtwA84n5yV0eV2wJYrd4i5DcKTAaw1VyN0Kwk/MryieT/RSMqX5xyJNUN4051BH4bw5hBvHf3NnRCeDM7/wBKkK5VMXUXellk6Fw1MuAVujtkL7X8MBH+312zbU/Ms2heGfN5cYR5AJ4IT/F5X1UqIAhmE5sjYIbOdTasubeCUZuygJ0MkEmN1JS8XEXpAJMK6fjNqD7kR2g1cFl6F/MqnaRdua367magAPsRYMbCVoFzAPJ2MlUsGnZSu0EDzCcLhLf+bP5lf5D5nAw3rY25I3WPehRBCCCFE3dMVyruOhiPMvuZfCK3EHyNMI7OcvI6wQsX+7BcQVcGGnjFId4Y2R7Hc/JqBqAhus+Z260qnALitexw0z5KKaaiuCb617JVR2CVoQLzWuFUQifyO8EDLRnQmRBE8jugU4oZMeXxSP4gCDYgfcl6+zaOojuaLFEYhLP2/i/qCZz/1gh9f5X8YD59VaBmcxaYV/m/yvSvvQ7jBoLmQ2QfCkw95j+aT8T0IT5o4elHvgz8t/dGnILx5wns0l/MHQ3hymkHzzDrW0H0hvNiYH7Aw8FE5M6i/kwE+g+9s2z9JKyw9UH8PyNnwmwC6aH4P0QKPE+LJlB6jwqEQRfC2GTvkTRCJ8GWXsULmua09IRLhA3Edag/5EkIlJ0rAVgJuB2QrcDUhc4JOIVcAJ9r4No+0gXOEPRDaV181PDObL5nny+b50nkGz42arFLYPLMEYbBXsg5/DStYlHeQjKqlAAAAAElFTkSuQmCC";

  var img$1 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAG4AAABuCAYAAADGWyb7AAAACXBIWXMAAG66AABuugHW3rEXAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAASjSURBVHgB7Z1LqFVVGMf/3RS1tIENCoqu0YNqUEJvekGFRFFRQSDUKBo1SKKGjUQIm0UvEAQdiIKKA0c6cCIq+ADFF/i4gvhG8YFvvfp9Lh143Xudczxn7/39N/8f/Cb3nHP3Put/ztl7r73W+gAhhBBCiEA8gP4YMp8zp5kPgpNR85y5zzyKFjPO/NycZ54yb7TE6+YWczbSB7FVvGNuB1cg9+MZ8w/zUbSAOeBq/EG433wRpEwyF4CrwQfpcfNNEPI7uBq6Co+YwyDifXA1cJWuAgnjzU3gatyqnQkCvgFXo9bhJgRhKPPYtxBjedV8AgEoC26C+TFEESE+0GXBeTfWVIgiXkMAyoJ7HKKMpxGA3E+lKGYcAjAEQYmCI0XBkaLgSFFwpCg4UhQcKQqOFAVHSohegC45b25DGkpwFek2SxMcQACiB7fbnG+uQRo6Nwpxi6jBeWA/mevMSxBd8xmaucPsI4p/RP8jrFtPpG/cCNII6R0QHYkS3FbzQ6Qh7UX4fn5kzjCnmw+D91t5BWmOwkZzpbkTA6TOn0rf8adK9sNvWv5lnq5xf+rWz5R9YNZAJs3UFZwPNH25ZB9+QLoEiNTIVboaAxh0W1dwswq27T+BfgkwWtM+RPKQ+Tr6oI7g/LhWdJz6u4ZtR/YC+phoUkdwXxZsd1YN22XQJ1mOx31QdXC7zIljtjmMdLYVqQGb9F9kaOpy4D/c2yPyi/lY5jWXzSVIM2H9E3kdnExBuqz5Dfmhft+bc9Fj32jV37i3xmxvsnkx8/wRpNmwbcJP//9Evp1+RY9UGZxP0x37M/lFh9e8gvbiF+Fl73s9eqTK4NYWbO+fzPMXoN28hHTYKHv/jxS9qIkbqacL/vZ85vnz0G6852ht5vHpRX9sIrii2zSTM8/fi/azL/NYYds0EVzRzdBcPx3r2WMvXEOPaMwJKQqOFAVHioIjRcGRouBIUXCkKDhSFBwpCo4UBUeKgiNFwZGi4EhRcKQoOFIUHCllwd2ACE1ZcOcgQlMW3FaI0JQFd9bcABGW3MnJMoiw5IJbijQjVAQkF9wBcyFESDpdx/2M/Chb0RCdgvM1s3ztkQsQoeim58Rnj36AIIuPiUS3XV5eDMhLkq2GCEEvfZUHzU/Mr5AWVREN0usccJ9ps+K2vqTDp0hlKL2ky8Qu/8ceiL7pZ/L+rtuKBtBtHVIUHCkKjhQFR4qCI0XBkaLgSFFwpCg4UhQcKQqOFAVHioIjhSE41dkpIEpwuRXypqD95N7jlaI/RgluJPPYDLSbh8z3Mo8fRmB8BfSypW29ltxA6s4ExUvRlL33k+YEBOZJ5Ndx/h/tPNZNM0+g/H0vBgE+kiwX3nL0UbokGF5G7Tvkl+x3vy77B5E+xTPNRR2e40UjfOFpH3DEWi/VVzX3Y9pwh+dtNt9GGpQcnlWobtl8Nt8FEc8g1ZRjauAqnA1C3jCPgauhB6kXyJgEUl5AqjfA1OCDcC5awFRzDtpdH/WOXom5bUWfbp19+W++Xy541xhLGJ30D6SXn/FpbD0X+WO7qPX6cs8i9e2x9qZ4VY8RpEMB6yWNEEIIIcTd3ARezpMsx1/oKAAAAABJRU5ErkJggg==";

  /** colors.ts: Supplies all the basic color functionalities used in the colorPicker. */
  /** From colors.coffee, used in NetLogo color conversions. */
  var colorTimesTen;
  var baseIndex;
  var r, g, b;
  var step;
  /** netlogoBaseColors: The array of NetLogo base colors in [r, g, b] form. */
  const netlogoBaseColors = [
      [140, 140, 140], // gray       (5)
      [215, 48, 39], // red       (15)
      [241, 105, 19], // orange    (25)
      [156, 109, 70], // brown     (35)
      [237, 237, 47], // yellow    (45)
      [87, 176, 58], // green     (55)
      [42, 209, 57], // lime      (65)
      [27, 158, 119], // turquoise (75)
      [82, 196, 196], // cyan      (85)
      [43, 140, 190], // sky       (95)
      [50, 92, 168], // blue     (105)
      [123, 78, 163], // violet   (115)
      [166, 25, 105], // magenta  (125)
      [224, 126, 149], // pink     (135)
      [0, 0, 0], // black
      [255, 255, 255], // white
  ];
  /** mappedColors: Maps the name of the base colors to their corresponding NetLogo representation*/
  var mappedColors = {
      gray: 5,
      red: 15,
      orange: 25,
      brown: 35,
      yellow: 45,
      green: 55,
      lime: 65,
      turquoise: 75,
      cyan: 85,
      sky: 95,
      blue: 105,
      violet: 115,
      magenta: 125,
      pink: 135,
  };
  /** cachedNetlogoColors: Returns [r, g, b] form of Netlogo colors in a 2d array. */
  let cachedNetlogoColors = (function () {
      var k, results;
      results = [];
      for (colorTimesTen = k = 0; k <= 1400; colorTimesTen = ++k) {
          baseIndex = Math.floor(colorTimesTen / 100);
          [r, g, b] = netlogoBaseColors[baseIndex];
          step = ((colorTimesTen % 100) - 50) / 50.48 + 0.012;
          if (step < 0) {
              r += Math.floor(r * step);
              g += Math.floor(g * step);
              b += Math.floor(b * step);
          }
          else {
              r += Math.floor((0xff - r) * step);
              g += Math.floor((0xff - g) * step);
              b += Math.floor((0xff - b) * step);
          }
          results.push([r, g, b]);
      }
      return results;
  })();
  /** componentToHex: Converts one component of a rgb color to its hex string. */
  function componentToHex(c) {
      const hex = c.toString(16);
      return hex.length == 1 ? "0" + hex : hex;
  }
  /** hexToRgb: converts a hex value to rgb  */
  function hexToRgb(hex) {
      let sanitizedHex = hex.replace(/^#/, '');
      // If it's a three-character hex code, convert to six-character
      if (sanitizedHex.length === 3) {
          sanitizedHex = sanitizedHex[0] + sanitizedHex[0] + sanitizedHex[1] + sanitizedHex[1] + sanitizedHex[2] + sanitizedHex[2];
      }
      const bigint = parseInt(sanitizedHex, 16);
      const r = (bigint >> 16) & 255;
      const g = (bigint >> 8) & 255;
      const b = bigint & 255;
      return [r, g, b];
  }
  // Consolidate RGB(A) to Hex conversion
  function colorToHex(r, g, b, a) {
      if (a !== undefined) {
          // Include alpha if provided
          let alpha = ((a | (1 << 8)).toString(16)).slice(1);
          return `#${componentToHex(r)}${componentToHex(g)}${componentToHex(b)}${alpha}`.toUpperCase();
      }
      else {
          return `#${componentToHex(r)}${componentToHex(g)}${componentToHex(b)}`.toUpperCase();
      }
  }
  function rgbToHex(r, g, b) {
      return colorToHex(r, g, b);
  }
  function rgbaToHex(r, g, b, a) {
      return colorToHex(r, g, b, a);
  }
  function rgbToNetlogo([r, g, b]) {
      if (r == 0 && g == 0 && b == 0) {
          return 0;
      }
      // Calculate the Euclidean distance between current color and each NetLogo color
      let minDistance = Infinity;
      let closestNetlogoColor = 0;
      for (let i = 0; i < cachedNetlogoColors.length; i++) {
          const [netR, netG, netB] = cachedNetlogoColors[i];
          const distance = Math.sqrt(Math.pow(r - netR, 2) + Math.pow(g - netG, 2) + Math.pow(b - netB, 2));
          if (distance < minDistance) {
              minDistance = distance;
              closestNetlogoColor = i;
          }
      }
      // Return the closest NetLogo color value
      return closestNetlogoColor / 10;
  }
  /** cached: 2d array of NetLogo colors in rgb form. */
  let cached = cachedNetlogoColors;
  /** netlogoColorToHex: Converts NetLogo color to its hex string. */
  function netlogoColorToHex(netlogoColor) {
      let temp = cached[Math.floor(netlogoColor * 10)];
      return rgbToHex(temp[0], temp[1], temp[2]);
  }
  /**
   * arrToString: takes an rgb(a) array and returns a string rgb(a)**/
  function arrToString(colorArray) {
      // Check if the array represents an RGBA color
      if (!Array.isArray(colorArray) || !colorArray.every(item => typeof item === 'number')) {
          console.error('Invalid colorArray input:', colorArray);
          return 'invalid'; // or any fallback string you prefer
      }
      if (colorArray.length === 4) {
          const [r, g, b, a] = colorArray;
          return `rgba(${r}, ${g}, ${b}, ${a / 255})`; //alpha defaults to values between 0 and 1 in css
      }
      // If not RGBA, assume it's RGB
      const [r, g, b] = colorArray;
      return `rgb(${r}, ${g}, ${b})`;
  }
  /** RGBAToHSBA: Converts rgba color to hsba color array. */
  function RGBAToHSBA(r, g, b, a) {
      // Normalize RGB values to [0, 1]
      r /= 255;
      g /= 255;
      b /= 255;
      const max = Math.max(r, g, b);
      const min = Math.min(r, g, b);
      const delta = max - min;
      let h = 0;
      let s = 0;
      const v = max;
      // calculate hue
      if (delta !== 0) {
          if (max === r) {
              h = ((g - b) / delta) % 6;
          }
          else if (max === g) {
              h = (b - r) / delta + 2;
          }
          else {
              h = (r - g) / delta + 4;
          }
          h = Math.round(h * 60);
          if (h < 0)
              h += 360;
      }
      s = max === 0 ? 0 : (delta / max) * 100;
      const b_percent = v * 100;
      // round values
      h = Math.round(h);
      s = Math.round(s);
      const v_percent = Math.round(b_percent);
      // Keep alpha in [0, 255] range
      a = Math.round(Math.max(0, Math.min(255, a)));
      return [h, s, v_percent, a];
  }
  /** HSBAToRGBA: Converts hsba color values to rgba color array. */
  function HSBAToRGBA(h, s, b, alpha) {
      // ensure h, s, and b are in the correct range
      h = Math.max(0, Math.min(360, h));
      s = Math.max(0, Math.min(100, s)) / 100;
      b = Math.max(0, Math.min(100, b)) / 100;
      const c = b * s;
      const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
      const m = b - c;
      let r, g, bl;
      if (h >= 0 && h < 60) {
          [r, g, bl] = [c, x, 0];
      }
      else if (h >= 60 && h < 120) {
          [r, g, bl] = [x, c, 0];
      }
      else if (h >= 120 && h < 180) {
          [r, g, bl] = [0, c, x];
      }
      else if (h >= 180 && h < 240) {
          [r, g, bl] = [0, x, c];
      }
      else if (h >= 240 && h < 300) {
          [r, g, bl] = [x, 0, c];
      }
      else {
          [r, g, bl] = [c, 0, x];
      }
      r = Math.round((r + m) * 255);
      g = Math.round((g + m) * 255);
      bl = Math.round((bl + m) * 255);
      return [r, g, bl, alpha];
  }
  /** netlogoColorToRGBA: Converts NetLogo color to rgba color array. */
  function netlogoColorToRGBA(netlogoColor, alpha = 255) {
      let temp = cached[Math.floor(netlogoColor * 10)];
      return [temp[0], temp[1], temp[2], alpha];
  }
  /** netlogoToCompound: Converts a numeric NetLogo Color to a compound color string */
  function netlogoToCompound(netlogoColor) {
      let baseColorIndex = Math.floor(netlogoColor / 10);
      let baseColorName = Object.keys(mappedColors)[baseColorIndex];
      // Calculate offset and immediately round to one decimal point
      let offset = Number(((netlogoColor % 10) - 5).toFixed(1));
      // if white return white
      if (netlogoColor == 9.9) {
          return 'white';
      }
      if (netlogoColor == 0) {
          return 'black';
      }
      if (offset === 0) {
          // If the color is a base color, return only the base color name
          return baseColorName;
      }
      else if (offset > 0) {
          return `${baseColorName} + ${offset}`;
      }
      else {
          return `${baseColorName} - ${Math.abs(offset)}`;
      }
  }

  /** ColorMode: Base class for each of the ColorPicker Modes */
  class ColorMode {
      /** constructor: sets the parent, state, and setState */
      constructor(parent, state, setState) {
          this.parent = parent;
          this.parent.innerHTML = '';
          this.state = state;
          this.setState = setState;
      }
      /** showNumbers: will show numbers in the mode */
      showNumbers() { }
      ;
      /** hideNumbers: nhide the numbers in the ColorPicker preview */
      hideNumbers() { }
      ;
  }

  class GridMode extends ColorMode {
      constructor(parent, state, setState) {
          super(parent, state, setState);
          /** colorArray: an array of netlogo colors in the grid */
          this.colorArray = [];
          /** rectArray: a 2D array of grid rects */
          this.rectArray = [];
          /** textElements: Array of SVGTextElements that are the "numbers" of each cell in the grid. */
          this.textElements = [];
          /** selectedRect: The currently selected SVGRectElement */
          this.selectedRect = null;
          this.init();
      }
      /** createGrid: creates the grid of colors */
      createGrid() {
          const colorsPerRow = 10 / this.state.increment + 1;
          const numRows = 14;
          const cellWidth = 21 / colorsPerRow;
          const cellHeight = 16.5 / numRows;
          const textFontSize = this.state.increment == 1 ? 0.6 : 0.4;
          const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
          svg.setAttribute('width', '100%');
          svg.setAttribute('height', '100%');
          svg.setAttribute('viewBox', '0 0 21 16.5');
          this.rectArray = []; // Initialize 2D rectArray
          // Event Handlers
          const hover = (e) => {
              if (e.target instanceof SVGRectElement && e.target !== this.selectedRect) {
                  const rect = e.target;
                  const value = Number(rect.dataset.value);
                  const hoverColor = (value % colorsPerRow < (colorsPerRow + 1) / 3) ? 'white' : 'black';
                  rect.setAttribute('stroke-width', '0.08');
                  rect.setAttribute('stroke', hoverColor);
              }
          };
          const hoverOut = (e) => {
              if (e.target instanceof SVGRectElement && e.target !== this.selectedRect) {
                  const rect = e.target;
                  rect.setAttribute('stroke-width', '');
                  rect.setAttribute('stroke', '');
              }
          };
          const handleChangeColor = (e) => {
              if (e.target instanceof SVGRectElement) {
                  const rect = e.target;
                  const colorIndex = Number(rect.dataset.value);
                  const newColor = netlogoColorToRGBA(this.colorArray[colorIndex]);
                  newColor[3] = this.state.currentColor[3];
                  this.setState({ currentColor: newColor, colorType: "netlogo" });
                  // Update selectedRect
                  if (this.selectedRect) {
                      // Remove gold border from previously selected rect
                      this.selectedRect.setAttribute('stroke-width', '');
                      this.selectedRect.setAttribute('stroke', '');
                      this.selectedRect.style.filter = 'none';
                  }
                  rect.style.filter = 'drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4))';
                  this.selectedRect = rect;
              }
          };
          // Create grid cells
          for (let j = 0; j < numRows; j++) {
              const row = []; // Create a new row
              for (let i = 0; i < colorsPerRow; i++) {
                  let number = j * 10 + i * this.state.increment;
                  if (i == colorsPerRow - 1) {
                      number -= 0.1;
                  }
                  this.colorArray.push(number);
                  const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
                  rect.classList.add('cp-grid-cell');
                  rect.setAttribute('x', `${cellWidth * i}`);
                  rect.setAttribute('y', `${cellHeight * j}`);
                  rect.setAttribute('width', `${cellWidth}`);
                  rect.setAttribute('height', `${cellHeight}`);
                  rect.setAttribute('fill', netlogoColorToHex(number));
                  rect.setAttribute('data-value', `${j * colorsPerRow + i}`);
                  rect.addEventListener('mouseover', hover);
                  rect.addEventListener('mouseout', hoverOut);
                  rect.addEventListener('click', handleChangeColor);
                  svg.appendChild(rect);
                  row.push(rect); // Add the rect to the current row
                  // Create and append text element for each rect
                  if (this.state.increment > 0.1) {
                      const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
                      text.setAttribute('x', `${cellWidth * i + cellWidth / 2}`);
                      text.setAttribute('y', `${cellHeight * j + cellHeight / 2}`);
                      text.setAttribute('fill', (i < (colorsPerRow + 1) / 3) ? 'white' : 'black');
                      text.setAttribute('dominant-baseline', 'middle');
                      text.setAttribute('text-anchor', 'middle');
                      text.classList.add('cp-grid-text');
                      text.textContent = `${number}`;
                      text.setAttribute('visibility', this.state.showNumbers ? 'visible' : 'hidden');
                      text.setAttribute('font-size', textFontSize.toString());
                      this.textElements.push(text);
                      svg.appendChild(text);
                  }
              }
              this.rectArray.push(row); // Add the row to the 2D rectArray
          }
          return svg;
      }
      /** toDOM: creates the body of the Grid */
      toDOM() {
          this.parent.innerHTML = '';
          const gridContainer = document.createElement('div');
          gridContainer.classList.add('cp-grid-cont');
          gridContainer.appendChild(this.createGrid());
          const spaceContainer = document.createElement('div');
          spaceContainer.classList.add("cp-space-container");
          spaceContainer.appendChild(gridContainer);
          const incrementBtns = `
        <div class="cp-grid-btn-cont">
            <div class="cp-increment-cont">
                <button class="cp-numbers-btn"></button>
                <span class="cp-increment-label">${Localized('Numbers')}</span>
            </div>
            <div class="cp-increment-cont">
                <div class="cp-btn-label-cont">
                    <button data-increment="1" class="cp-numbers-btn cp-numbers-clicked"></button>
                    <span class="cp-increment-label">1</span>
                </div>
                <div class="cp-btn-label-cont">
                    <button data-increment="0.5" class="cp-numbers-btn"></button>
                    <span class="cp-increment-label">0.5</span>
                </div>
                <div class="cp-btn-label-cont">
                    <button data-increment="0.1" class="cp-numbers-btn"></button>
                    <span class="cp-increment-label">0.1</span>
                </div>
                <span class="cp-increment-label">${Localized('Increment')}</span>
            </div>
        </div>
        `;
          spaceContainer.insertAdjacentHTML('beforeend', incrementBtns);
          this.parent.appendChild(spaceContainer);
      }
      /** updateIncrementAppearance: updates the increment button appearance based on which increment is on */
      updateIncrementAppearance() {
          var _a;
          const incrementBtns = this.parent.querySelectorAll('.cp-numbers-btn');
          incrementBtns[0].classList.toggle('cp-numbers-clicked', this.state.showNumbers);
          for (let i = 1; i < incrementBtns.length; i++) {
              const btn = incrementBtns[i];
              const incrementValue = parseFloat((_a = btn.getAttribute('data-increment')) !== null && _a !== void 0 ? _a : "0");
              const isSelected = incrementValue === this.state.increment;
              btn.classList.toggle('cp-numbers-clicked', isSelected);
          }
      }
      /** attachEventListeners: Attaches the event listeners to the GridMode body */
      attachEventListeners() {
          const gridBtns = this.parent.querySelectorAll('.cp-numbers-btn');
          // Event listener for the numbers button 
          gridBtns[0].addEventListener('click', () => {
              this.setState({ showNumbers: !this.state.showNumbers });
              this.toggleTextVisibility();
              this.updateIncrementAppearance();
          });
          // Event listeners for the increment buttons 
          for (let i = 1; i < gridBtns.length; i++) {
              gridBtns[i].addEventListener('click', () => {
                  var _a;
                  const increment = parseFloat((_a = gridBtns[i].getAttribute('data-increment')) !== null && _a !== void 0 ? _a : "0");
                  this.setState({ increment: increment });
                  // Reset the colorArray and reinitialize the grid
                  this.colorArray = [];
                  this.init();
              });
          }
      }
      /** toggleTextVisibility: toggles the text visibility based on state of numbers */
      toggleTextVisibility() {
          const visibility = this.state.showNumbers ? 'visible' : 'hidden';
          this.textElements.forEach((text) => text.setAttribute('visibility', visibility));
      }
      /** init: initializes a grid mode  */
      init() {
          this.toDOM();
          this.setSelectedRect();
          this.updateIncrementAppearance();
          this.attachEventListeners();
      }
      /** setSelectedRect: sets the selectedRect at the beginning, based on the closest netlogo color */
      setSelectedRect() {
          if (this.rectArray.length > 0) {
              const colorsPerRow = this.rectArray[0].length;
              const netlogoColor = rgbToNetlogo(this.state.currentColor.slice(0, 3));
              console.log(`Calculated NetLogo color: ${netlogoColor}`);
              let closestColorIndex = 0;
              let smallestDifference = Infinity;
              this.colorArray.forEach((color, index) => {
                  const difference = Math.abs(color - netlogoColor);
                  if (difference < smallestDifference) {
                      smallestDifference = difference;
                      closestColorIndex = index;
                  }
              });
              const rowIndex = Math.floor(closestColorIndex / colorsPerRow);
              const colIndex = closestColorIndex % colorsPerRow;
              const closestRect = this.rectArray[rowIndex][colIndex];
              if (this.selectedRect) {
                  this.selectedRect.setAttribute('stroke-width', '');
                  this.selectedRect.setAttribute('stroke', '');
                  this.selectedRect.style.filter = 'none';
              }
              const value = Number(closestRect.dataset.value);
              const strokeColor = (value % colorsPerRow < (colorsPerRow + 1) / 3) ? 'white' : 'black';
              closestRect.style.filter = 'drop-shadow(3px 5px 2px rgb(0 0 0 / 0.4))';
              closestRect.setAttribute('stroke-width', '0.08');
              closestRect.setAttribute('stroke', strokeColor);
              this.selectedRect = closestRect;
          }
      }
  }

  /** dragHelpers.ts: defines helper functions useful for the dragging and clicking feature of the Wheel Mode */
  /** getMousePosition: Gets the mouse position of an MouseEvent(evt) in an SVG element(svg) */
  function getMousePosition(evt, svg) {
      evt = evt; // standardize evt as MouseEvent
      let CTM = svg.getScreenCTM();
      if (CTM != null) {
          return {
              x: (evt.clientX - CTM.e) / CTM.a,
              y: (evt.clientY - CTM.f) / CTM.d,
          };
      }
  }
  /** getTouchPosition: get the position of the touch event */
  function getTouchPosition(evt, svg) {
      const touch = evt.touches[0];
      const CTM = svg.getScreenCTM();
      if (CTM != null) {
          return {
              x: (touch.clientX - CTM.e) / CTM.a,
              y: (touch.clientY - CTM.f) / CTM.d
          };
      }
      return null;
  }
  /** distance: Calculates the distance between two points with coordinates (x1, y1), and (x2, y2) */
  function distance(x1, y1, x2, y2) {
      return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
  }
  /** findAngle: Calculates the angle between three points with coordinates (a, b), (c, d), and (e, f) */
  function findAngle(a, b, c, d, e, f) {
      let AB = Math.sqrt(Math.pow(c - a, 2) + Math.pow(d - b, 2));
      let BC = Math.sqrt(Math.pow(c - e, 2) + Math.pow(d - f, 2));
      let AC = Math.sqrt(Math.pow(e - a, 2) + Math.pow(f - b, 2));
      let outOf180Degrees = Math.acos((BC * BC + AB * AB - AC * AC) / (2 * BC * AB)) *
          (180 / Math.PI);
      // if we are "positive" relative to the axis -- the center point to the top "zero" point, then we just return, else we return 360 - outOf180
      if (e < c) {
          return 360 - outOf180Degrees;
      }
      return outOf180Degrees;
  }

  /** GridMode: A mode for the ColorPicker that shows a wheel of colors */
  class WheelMode extends ColorMode {
      constructor(parent, state, setState) {
          super(parent, state, setState);
          /** textElements: Array of SVGTextElements that are the "numbers" of each cell in the grid. */
          this.textElements = [];
          this.outerWheelColors = []; // hex colors of the outer wheel
          this.init();
      }
      /** toDOM: creates the body of the Wheel */
      toDOM() {
          this.parent.innerHTML = `
        <div class="cp-wheel-spacing-cont"><div class="cp-wheel-cont"><svg class="cp-wheel-svg" viewBox="0 0 110 110" width="100%" height="100%">
        <clipPath id="cp-inner-wheel-clip">
        <path d="M 55 35 
                 a 20 20 0 0 1 0 40 
                 a 20 20 0 0 1 0 -40 
                 M 55 10 
                 a 45 45 0 0 0 0 90 
                 a 45 45 0 0 0 0 -90"></path>
    </clipPath>

        <clipPath id="cp-outer-wheel-clip">
            <path d="M 55 0 
                        a 55 55 0 0 1 0 110 
                        a 55 55 0 0 1 0 -110 
                        M 55 5 
                        a 50 50 0 0 0 0 100 
                        a 50 50 0 0 0 0 -100"></path>
        </clipPath>
    

        <foreignObject width="110" height="110" clip-path="url(#cp-inner-wheel-clip)" >
            <div xmlns="http://www.w3.org/1999/xhtml" class="cp-inner-wheel"></div>
        </foreignObject>

        <foreignObject width="110" height="110" clip-path="url(#cp-outer-wheel-clip)">
            <div xmlns="http://www.w3.org/1999/xhtml" class="cp-outer-wheel"></div>
        </foreignObject>

    </svg></div><div class="cp-grid-btn-cont"><div class="cp-increment-cont"><button class="cp-numbers-btn"></button><span class="cp-increment-label">${Localized('Numbers')}</span></div><div class="cp-increment-cont"><div class="cp-btn-label-cont"><button data-increment="1" class="cp-numbers-btn cp-numbers-clicked"></button><span class="cp-increment-label">1</span></div><div class="cp-btn-label-cont"><button data-increment="0.5" class="cp-numbers-btn"></button><span class="cp-increment-label">0.5</span></div><div class="cp-btn-label-cont"><button data-increment="0.1" class="cp-numbers-btn"></button><span class="cp-increment-label">0.1</span></div><span class="cp-increment-label">${Localized('Increment')}</span></div></div></div>
        `;
          this.innerWheelSetup();
          this.outerWheelSetup(Math.floor(rgbToNetlogo(this.state.currentColor) / 10) * 10);
          this.numbersSetup();
      }
      /** innerWheelSetup() : sets up the color of the inner wheel  */
      innerWheelSetup() {
          // get the inner wheel 
          const innerWheel = document.querySelector('.cp-inner-wheel');
          let netlogoColors = Object.keys(mappedColors);
          let hexColors = [];
          for (let i = 0; i < netlogoColors.length; i++) {
              hexColors.push(netlogoColorToHex(Number(mappedColors[netlogoColors[i]])));
          }
          let degreesPerSV = 360 / netlogoColors.length;
          let cssFormat = `background-image: conic-gradient(`;
          let degreeTracker = 0;
          for (let i = 0; i < netlogoColors.length - 1; i++) {
              cssFormat +=
                  hexColors[i] +
                      ` ${degreeTracker}deg ${degreeTracker + degreesPerSV}deg, `;
              degreeTracker += degreesPerSV;
          }
          cssFormat +=
              hexColors[netlogoColors.length - 1] + ` ${degreeTracker}deg 0deg`;
          innerWheel.setAttribute('style', cssFormat + `);`);
      }
      /** setInner: takes the current NetLogo color and places the draggable thumbs in the approximate location they should be  */
      setInner() {
          let radius, degreesPerIncrement, baseColorIndex, angle;
          let center = [55, 55];
          radius = 30; // inner thumb is going to be here
          degreesPerIncrement = 360 / 14;
          const netlogoColor = rgbToNetlogo([this.state.currentColor[0], this.state.currentColor[1], this.state.currentColor[2]]);
          baseColorIndex = Math.floor(netlogoColor / 10);
          angle = baseColorIndex * degreesPerIncrement + degreesPerIncrement / 2;
          let angleInRadians = (angle * Math.PI) / 180;
          let x = center[0] + radius * Math.sin(angleInRadians);
          let y = center[1] - radius * Math.cos(angleInRadians);
          return [x, y];
      }
      /** setOuter: takes the current NetLogo color and places the draggable thumb in the approximate lcoaiton */
      setOuter() {
          let radius, degreesPerIncrement, angle;
          let center = [55, 55];
          radius = 52.5;
          degreesPerIncrement = 360 / (10 / this.state.increment + 1);
          const netLogoColor = rgbToNetlogo([this.state.currentColor[0], this.state.currentColor[1], this.state.currentColor[2]]);
          let value = netLogoColor % 10;
          if (Math.abs(value - 9.9) < 0.00001)
              value = 10;
          let index = Math.floor(value / this.state.increment);
          angle = index * degreesPerIncrement + degreesPerIncrement / 2;
          let angleInRadians = (angle * Math.PI) / 180;
          let x = center[0] + radius * Math.sin(angleInRadians);
          let y = center[1] - radius * Math.cos(angleInRadians);
          return [x, y];
      }
      /** toRadians: Coverts degress to radians  */
      toRadians(degrees) {
          return degrees * Math.PI / 180;
      }
      /** numbersSetup: sets up & updates the appropriate numbers for the wheel */
      numbersSetup() {
          const radius = 38;
          const degreesPerIncrement = 360 / 14; // divide by 14 because the wheel has 14 inner colors 
          const center = [55, 55];
          const svg = document.querySelector(".cp-wheel-svg");
          if (svg === null) {
              return;
          }
          /** calculate the correct x and y coords in the svg viewbox for each text element  */
          for (let i = 0; i < 14; i++) {
              let angle = this.toRadians(i * degreesPerIncrement + degreesPerIncrement / 2) - 0.01; // give it a small offset for appearance
              const x = center[0] + radius * Math.sin(angle); // Use sin for x
              const y = center[1] - radius * Math.cos(angle); // Use -cos for y
              let text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
              text.setAttribute('x', `${x}`);
              text.setAttribute('y', `${y}`);
              text.setAttribute('fill', 'white');
              text.setAttribute('font-size', '0.4rem');
              text.setAttribute("dominant-baseline", "middle");
              text.setAttribute("text-anchor", "middle");
              text.setAttribute('visibility', this.state.showNumbers ? 'visible' : 'hidden');
              text.textContent = `${i * 10 + 5}`;
              this.textElements.push(text);
              svg.appendChild(text);
          }
          // create text elements for the outer wheel 
          const numIncrements = 10 / this.state.increment + 1;
          const degreesPerIncrementOuter = 360 / numIncrements;
          const outerRadius = 55;
          const arcOffsetFactor = (this.state.increment == 1) ? 1.75 : 1.6;
          let angle;
          let angleInRadians;
          for (let i = 0; i < numIncrements + 1; i++) {
              angle = i * degreesPerIncrementOuter + degreesPerIncrementOuter / arcOffsetFactor;
              angleInRadians = this.toRadians(angle);
              const x = center[0] + outerRadius * Math.sin(angleInRadians);
              const y = center[1] - outerRadius * Math.cos(angleInRadians);
              const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
              text.setAttribute('x', `${x}`);
              text.setAttribute('y', `${y}`);
              text.setAttribute('text-anchor', 'middle');
              const textOffsetX = x + 2;
              const textOffsetY = y - 2;
              text.setAttribute('transform', `rotate(${angle}, ${textOffsetX}, ${textOffsetY})`);
              text.classList.add('cp-wheel-numbers');
              if (i > (numIncrements / 3)) {
                  text.style.fill = "black";
                  text.style.fontSize = '1rem';
              }
              this.textElements.push(text);
              svg.appendChild(text);
          }
      }
      /** updateInnerWheel: Updates the color of the wheel based on the location of the inner thumb */
      updateInnerWheel(inner) {
          let innerX = Number(inner.getAttribute('cx'));
          let innerY = Number(inner.getAttribute('cy'));
          const angle = findAngle(55, 20, 55, 55, innerX, innerY);
          let degreesPerIndex = 360 / 14; // the number of degrees per slice of the inner wheel
          let innerColor = netlogoBaseColors[Math.floor(angle / degreesPerIndex)];
          inner.setAttribute('fill', `rgba(${innerColor[0]}, ${innerColor[1]}, ${innerColor[2]}, 255)`);
          this.outerWheelSetup(Math.floor(angle / degreesPerIndex) * 10);
      }
      /** setThumbs: creates the thumbs and sets them in the right spot  */
      setThumbs() {
          let innerThumb = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
          let innerThumbCor = this.setInner();
          innerThumb.setAttribute('cx', `${innerThumbCor[0]}`);
          innerThumb.setAttribute('cy', `${innerThumbCor[1]}`);
          innerThumb.setAttribute('r', '2');
          innerThumb.setAttribute('fill', 'black');
          innerThumb.setAttribute('stroke', 'white');
          innerThumb.setAttribute('stroke-width', '1.2');
          innerThumb.classList.add("cp-inner-thumb");
          innerThumb.classList.add("cp-draggable");
          let outerThumbCor = this.setOuter();
          let outerThumb = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
          outerThumb.setAttribute('cx', `${outerThumbCor[0]}`);
          outerThumb.setAttribute('cy', `${outerThumbCor[1]}`);
          outerThumb.setAttribute('r', '2');
          outerThumb.setAttribute('fill', 'orange');
          outerThumb.setAttribute("stroke", "white");
          outerThumb.setAttribute('stroke-width', '1.2');
          outerThumb.classList.add("cp-outer-thumb");
          outerThumb.classList.add("cp-draggable");
          // Create larger invisible circle for inner thumb
          let innerThumbHitArea = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
          innerThumbHitArea.setAttribute('cx', `${innerThumbCor[0]}`);
          innerThumbHitArea.setAttribute('cy', `${innerThumbCor[1]}`);
          innerThumbHitArea.setAttribute('r', '10'); // Larger radius
          innerThumbHitArea.setAttribute('fill', 'transparent');
          innerThumbHitArea.classList.add("cp-inner-thumb-hit-area");
          innerThumbHitArea.classList.add("cp-draggable");
          // Create larger invisible circle for outer thumb
          let outerThumbHitArea = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
          outerThumbHitArea.setAttribute('cx', `${outerThumbCor[0]}`);
          outerThumbHitArea.setAttribute('cy', `${outerThumbCor[1]}`);
          outerThumbHitArea.setAttribute('r', '20'); // Larger radius
          outerThumbHitArea.setAttribute('fill', 'transparent');
          outerThumbHitArea.classList.add("cp-outer-thumb-hit-area");
          outerThumbHitArea.classList.add("cp-draggable");
          const svg = document.querySelector('.cp-wheel-svg');
          svg.appendChild(innerThumbHitArea);
          svg.appendChild(outerThumbHitArea);
          svg.appendChild(innerThumb);
          svg.appendChild(outerThumb);
          const innerAsSVG = innerThumb;
          this.updateInnerWheel(innerAsSVG);
          this.changeColor(); // update the outer wheel 
      }
      /** changeColor: changes the color based on the position of the outerwheel */
      changeColor() {
          // calculate the outer thumb angle
          const outerThumb = document.querySelector(".cp-outer-thumb");
          if (outerThumb) {
              const outerX = Number(Number(outerThumb.getAttribute('cx')).toFixed(4));
              const outerY = Math.round(Number(outerThumb.getAttribute('cy')));
              const angle = findAngle(50, 20, 50, 50, outerX, outerY);
              const degreesPerIndex = 360 / (10 / this.state.increment + 1);
              const index = Math.floor(angle / degreesPerIndex);
              const color = this.outerWheelColors[index];
              outerThumb.setAttribute('fill', color);
              // set the color to the current color 
              const colorAsRGB = hexToRgb(color);
              const colorAsRGBA = [colorAsRGB[0], colorAsRGB[1], colorAsRGB[2], this.state.currentColor[3]];
              this.setState({ currentColor: colorAsRGBA, colorType: "netlogo" });
          }
      }
      /** outerWheelSetup(): sets up the color of the outer wheel */
      outerWheelSetup(baseColor) {
          // solve base color based on current color
          const numColors = 10 / this.state.increment + 1;
          this.outerWheelColors = [];
          for (let i = 0; i < numColors - 1; i++) {
              this.outerWheelColors.push(netlogoColorToHex(baseColor + i * this.state.increment));
          }
          this.outerWheelColors.push(netlogoColorToHex(baseColor + 9.9));
          const degreesPerSV = 360 / numColors;
          let cssFormat = `background-image: conic-gradient(`;
          let degreeTracker = 0;
          for (let i = 0; i < numColors - 1; i++) {
              cssFormat +=
                  this.outerWheelColors[i] +
                      ` ${degreeTracker}deg ${degreeTracker + degreesPerSV}deg, `;
              degreeTracker += degreesPerSV;
          }
          cssFormat +=
              this.outerWheelColors[numColors - 1] + ` ${degreeTracker}deg 0deg`;
          const outerWheel = document.querySelector('.cp-outer-wheel');
          outerWheel.setAttribute('style', cssFormat + `);`);
          // also update the color based on the changed outer wheel since the color will be different 
          this.changeColor();
      }
      /** makeDraggable(): makes the thumbs of the color wheel draggable */
      makeDraggable(wheel) {
          document.querySelector(".cp-inner-thumb");
          const cpWindow = document.querySelector(".cp");
          const center = [55, 55];
          /** throttle function to reduce number of event listener calls */
          function throttle(func, limit) {
              let inThrottle;
              return function () {
                  const args = arguments;
                  const context = this;
                  if (!inThrottle) {
                      func.apply(context, args);
                      inThrottle = true;
                      setTimeout(() => inThrottle = false, limit);
                  }
              };
          }
          function makeDraggable(cpWindow) {
              cpWindow.addEventListener("mousedown", startDrag);
              cpWindow.addEventListener("mousemove", throttle(drag, 1));
              cpWindow.addEventListener("mouseup", endDrag);
              cpWindow.addEventListener("mouseleave", endDrag);
              cpWindow.addEventListener("touchstart", startDrag, { passive: false });
              cpWindow.addEventListener("touchmove", throttle(drag, 1), { passive: false });
              cpWindow.addEventListener("touchend", endDrag);
              cpWindow.addEventListener("touchcancel", endDrag);
              let svg = document.querySelector(".cp-wheel-svg");
              let selectedElement;
              /** makeClickable: makes the inner and outer wheels clickable to move the thumb to that location */
              function makeClickable() {
                  const innerThumbHitArea = document.querySelector('.cp-inner-thumb-hit-area');
                  const outerThumbHitArea = document.querySelector('.cp-outer-thumb-hit-area');
                  const svg = document.querySelector(".cp-wheel-svg");
                  svg.addEventListener('click', (evt) => {
                      const pos = getMousePosition(evt, svg);
                      if (pos != null) {
                          const dist = distance(pos.x, pos.y, center[0], center[1]);
                          if (dist <= 42 && dist >= 20) {
                              // Inner wheel area
                              innerThumbHitArea === null || innerThumbHitArea === void 0 ? void 0 : innerThumbHitArea.setAttribute('cx', pos.x.toString());
                              innerThumbHitArea === null || innerThumbHitArea === void 0 ? void 0 : innerThumbHitArea.setAttribute('cy', pos.y.toString());
                              let innerThumb = document.querySelector('.cp-inner-thumb');
                              innerThumb.setAttribute('cx', pos.x.toString());
                              innerThumb.setAttribute('cy', pos.y.toString());
                              wheel.updateInnerWheel(innerThumb);
                          }
                          else if (dist >= 52.5 && dist <= 55) {
                              // Outer wheel area
                              const confined = confinementOuter(pos.x, pos.y);
                              outerThumbHitArea === null || outerThumbHitArea === void 0 ? void 0 : outerThumbHitArea.setAttribute('cx', confined.x.toString());
                              outerThumbHitArea === null || outerThumbHitArea === void 0 ? void 0 : outerThumbHitArea.setAttribute('cy', confined.y.toString());
                              let outerThumb = document.querySelector('.cp-outer-thumb');
                              outerThumb.setAttribute('cx', confined.x.toString());
                              outerThumb.setAttribute('cy', confined.y.toString());
                              wheel.changeColor();
                          }
                      }
                  });
              }
              makeClickable();
              /** startDrag: start drag event for draggable elements */
              function startDrag(evt) {
                  let element = evt.target;
                  // check if the clicked element is a draggable element or a thumb
                  if (element.classList.contains('cp-draggable') ||
                      element.classList.contains('cp-inner-thumb') ||
                      element.classList.contains('cp-outer-thumb')) {
                      evt.preventDefault();
                      // tf it's a thumb, select its corresponding hit area
                      if (element.classList.contains('cp-inner-thumb')) {
                          selectedElement = document.querySelector('.cp-inner-thumb-hit-area');
                      }
                      else if (element.classList.contains('cp-outer-thumb')) {
                          selectedElement = document.querySelector('.cp-outer-thumb-hit-area');
                      }
                      else {
                          selectedElement = element;
                      }
                  }
              }
              function confinementInner(x, y) {
                  let xRestrict = x;
                  let yRestrict = y;
                  let angle = findAngle(55, 20, 55, 55, x, y); // Given the reference point [55, 20] (straight up), and [55, 55] the center
                  const angleInRadians = wheel.toRadians(angle);
                  const innerLowerBound = 20;
                  const innerUpperBound = 42;
                  const dist = distance(x, y, center[0], center[1]);
                  if (dist > innerUpperBound) {
                      xRestrict = center[0] + innerUpperBound * Math.sin(angleInRadians);
                      yRestrict = center[1] - innerUpperBound * Math.cos(angleInRadians);
                  }
                  else if (dist < innerLowerBound) {
                      xRestrict = center[0] + innerLowerBound * Math.sin(angleInRadians);
                      yRestrict = center[1] - innerLowerBound * Math.cos(angleInRadians);
                  }
                  return { x: xRestrict, y: yRestrict };
              }
              function confinementOuter(x, y) {
                  let xRestrict = x;
                  let yRestrict = y;
                  const angle = findAngle(55, 20, 55, 55, x, y);
                  const angleInRadians = wheel.toRadians(angle);
                  const outerLowerBound = 52.5;
                  xRestrict = center[0] + outerLowerBound * Math.sin(angleInRadians);
                  yRestrict = center[1] - outerLowerBound * Math.cos(angleInRadians);
                  return { x: xRestrict, y: yRestrict };
              }
              function drag(evt) {
                  if (selectedElement != null) {
                      evt.preventDefault();
                      let mousePosition = (evt instanceof MouseEvent)
                          ? getMousePosition(evt, svg)
                          : getTouchPosition(evt, svg);
                      if (mousePosition != null) {
                          let x, y, restrict;
                          let isInner = selectedElement.classList.contains('cp-inner-thumb-hit-area');
                          let isOuter = selectedElement.classList.contains('cp-outer-thumb-hit-area');
                          if (isInner || isOuter) {
                              restrict = isInner ? confinementInner(mousePosition.x, mousePosition.y)
                                  : confinementOuter(mousePosition.x, mousePosition.y);
                              x = restrict.x;
                              y = restrict.y;
                              // Move the hit area
                              selectedElement.setAttribute('cx', x.toString());
                              selectedElement.setAttribute('cy', y.toString());
                              // Move the corresponding visible thumb
                              let visibleThumb = document.querySelector(isInner ? '.cp-inner-thumb' : '.cp-outer-thumb');
                              visibleThumb.setAttribute('cx', x.toString());
                              visibleThumb.setAttribute('cy', y.toString());
                              if (isInner) {
                                  wheel.updateInnerWheel(visibleThumb);
                              }
                              else {
                                  wheel.changeColor();
                              }
                          }
                      }
                  }
              }
              /** endDrag: ends the drag event for draggable elements */
              function endDrag(evt) {
                  selectedElement = null;
              }
          }
          if (cpWindow) {
              makeDraggable(cpWindow);
          }
      }
      /** updateIncrementApperance: updates the increment button apperance based on which increment is on */
      updateIncrementAppearance() {
          var _a;
          const incrementBtns = document.querySelectorAll('.cp-numbers-btn');
          incrementBtns[0].classList.toggle('cp-numbers-clicked', this.state.showNumbers);
          for (let i = 1; i < incrementBtns.length; i++) {
              const btn = incrementBtns[i];
              // Retrieve the data-increment value
              const incrementValue = parseFloat((_a = btn.getAttribute('data-increment')) !== null && _a !== void 0 ? _a : "0");
              const isSelected = incrementValue === this.state.increment;
              btn.classList.toggle('cp-numbers-clicked', isSelected);
          }
      }
      /** attachEventListeners: Attaches the event listeners to the GridMode body */
      attachEventListeners() {
          const gridBtns = document.querySelectorAll('.cp-numbers-btn');
          // event listener of the numbers button 
          gridBtns[0].addEventListener('click', () => {
              this.setState({ showNumbers: !this.state.showNumbers });
              this.toggleTextVisibility();
              this.updateIncrementAppearance();
          });
          // the increment buttons 
          for (let i = 1; i < gridBtns.length; i++) {
              gridBtns[i].addEventListener('click', () => {
                  var _a;
                  let increment = parseFloat((_a = gridBtns[i].getAttribute('data-increment')) !== null && _a !== void 0 ? _a : "0");
                  this.setState({ increment: increment });
                  this.state.increment = increment; // weird bug, did we create a copy of the state?
                  this.init();
              });
          }
      }
      /** toggleTextVisibility: toggles the text visibility based on state of numbers */
      toggleTextVisibility() {
          this.state.showNumbers ? this.textElements.forEach((text) => text.setAttribute('visibility', 'visible')) : this.textElements.forEach((text) => text.setAttribute('visibility', 'hidden'));
      }
      /** init: initializes a wheel mode  */
      init() {
          this.toDOM();
          this.updateIncrementAppearance();
          this.attachEventListeners();
          this.setThumbs();
          this.makeDraggable(this);
      }
  }

  class Slider {
      constructor(parent, startValue, min, max, sliderColor, sliderWidth, text, onValueChange, // callback function to be called after slider / inputbox value change 
      hasDisplay = true) {
          this.valueDisplayElement = null; // the input Element defining the value indicator
          this.sliderChangedValue = () => {
              const value = parseInt(this.inputElement.value);
              this.inputElement.style.setProperty('--value', value.toString());
              // color value has changed so call onValueChange
              this.onValueChange(value);
              return value.toString();
          };
          let r = document.querySelector(':root');
          r.style.setProperty('--slider', sliderColor);
          this.parent = parent;
          this.onValueChange = onValueChange;
          // Create the slider
          this.inputElement = document.createElement('input');
          this.inputElement.type = 'range';
          this.inputElement.style.width = sliderWidth;
          this.inputElement.value = startValue.toString();
          this.inputElement.min = min.toString();
          this.inputElement.max = max.toString();
          this.inputElement.classList.add('cp-styled-slider');
          this.inputElement.value = startValue.toString();
          if (sliderColor === 'alpha') {
              this.inputElement.classList.add('alpha-slider');
          }
          else {
              this.inputElement.classList.add('color-slider');
          }
          // Add color to slider TRACK
          this.inputElement.classList.add(`color-${sliderColor.toLowerCase()}`, 'slider-progress');
          this.inputElement.addEventListener('input', this.rangeSlide.bind(this));
          // Create a div to hold the slider
          let sliderContainer = document.createElement('div');
          sliderContainer.classList.add('cp-slider-container');
          // Create the text element
          if (text !== '') {
              let textElement = document.createElement('div');
              textElement.innerHTML = text;
              textElement.classList.add('cp-slider-text');
              sliderContainer.appendChild(textElement);
          }
          sliderContainer.appendChild(this.inputElement);
          // Create a div to hold slider and display
          let sliderDisplayContainer = document.createElement('div');
          sliderDisplayContainer.classList.add('cp-slider-display-container');
          sliderDisplayContainer.appendChild(sliderContainer);
          if (hasDisplay) {
              // create value-display: the input element that shows the value of the slider. 
              this.valueDisplayElement = document.createElement('input');
              this.valueDisplayElement.classList.add('cp-slider-value-display-cont');
              this.valueDisplayElement.inputMode = 'numeric';
              this.valueDisplayElement.type = 'number';
              this.valueDisplayElement.min = '0';
              this.valueDisplayElement.max = max.toString();
              this.valueDisplayElement.value = startValue.toString();
              sliderDisplayContainer.appendChild(this.valueDisplayElement);
              // add event listener for this input element as well to change the color 
              this.valueDisplayElement.addEventListener('input', (event) => {
                  const input = event.target;
                  const value = input.value;
                  // Allow empty input (will be treated as 0)
                  if (value === '') {
                      this.setValue(0);
                      // color value has changed so call onValueChange
                      this.onValueChange(0);
                      return;
                  }
                  let numValue = parseInt(value);
                  // If the input is not a number, reset to the previous valid value
                  if (isNaN(numValue)) {
                      input.value = this.inputElement.value.toString();
                      return;
                  }
                  numValue = Math.max(0, Math.min(max, numValue));
                  // Update the input value and the slider
                  input.value = numValue.toString();
                  this.setValue(numValue);
                  // color value has changed so call onValueChange
                  this.onValueChange(numValue);
              });
          }
          this.parent.appendChild(sliderDisplayContainer);
          this.finalize();
      }
      rangeSlide(event) {
          const target = event.target;
          let val = target.value;
          if (this.valueDisplayElement !== null) {
              this.valueDisplayElement.value = val;
          }
          // color value has changed so call onValueChange
          this.onValueChange(Number(val));
      }
      finalize() {
          let e = this.inputElement;
          e.style.setProperty('--value', e.value);
          e.style.setProperty('--min', e.min === '' ? '0' : e.min);
          e.style.setProperty('--max', e.max === '' ? '255' : e.max);
          e.addEventListener('input', this.sliderChangedValue.bind(this));
      }
      setValue(value) {
          const min = parseFloat(this.inputElement.min);
          const max = parseFloat(this.inputElement.max);
          value = Math.min(Math.max(value, min), max);
          this.inputElement.value = value.toString();
          if (this.valueDisplayElement !== null) {
              this.valueDisplayElement.value = value.toString();
          }
          this.inputElement.style.setProperty('--value', value.toString());
          // color value has changed so call onValueChange
          this.onValueChange(value);
      }
      getValue() {
          return parseFloat(this.inputElement.value);
      }
  }

  /** SliderMode: A mode for the ColorPicker that shows sliders for color adjustment */
  class SliderMode extends ColorMode {
      /**
       * Constructor for SliderMode
       * @param parent - The parent HTML element
       * @param state - The current state of the color picker
       * @param setState - Function to update the state
       * @param colorPickerInstance - Instance of the ColorPicker
       * @param mode - Initial mode, either 'rgb' or 'hsb' (optional, defaults to 'rgb')
       */
      constructor(parent, state, setState, colorPickerInstance, mode = 'rgb' // New parameter with default value
      ) {
          super(parent, state, setState);
          this.HSBA = [0, 0, 0, 0];
          this.sliders = [];
          this.isRGB = mode === 'rgb'; // Set initial mode based on the parameter
          this.init();
      }
      /** toDOM: creates the body of the SliderMode */
      toDOM() {
          this.parent.innerHTML = `
            <div class="cp-slider-cont">
                <div class="cp-slider-color-display"></div>
                <div class="cp-sliders">
                    <div class="cp-slider-change-mode"></div>
                    <!-- part that switches from rgb to hsb -->
                </div>
                <div class="cp-saved-colors-cont">
                    <div class="cp-saved-colors"></div>
                    <div class="cp-saved-colors"></div>
                    <div class="cp-saved-colors"></div>
                    <div class="cp-saved-colors"></div>
                    <div class="cp-saved-color-add"></div>
                </div>
            </div>
        `;
      }
      /** updateColorDisplay: updates the color display to be the current color  */
      updateColorDisplay() {
          const colorDisplay = document.querySelector('.cp-slider-color-display');
          colorDisplay.style.backgroundColor = `rgba(${this.state.currentColor[0]}, ${this.state.currentColor[1]}, ${this.state.currentColor[2]}, ${this.state.currentColor[3] / 255})`;
      }
      /** setupSavedColors: sets up saved colors by adding event handlers */
      setupSavedColors() {
          const addButton = document.querySelector(".cp-saved-color-add");
          addButton === null || addButton === void 0 ? void 0 : addButton.addEventListener("click", () => {
              const savedColors = [...this.state.savedColors]; // Clone to avoid direct mutation
              const colorCopy = [...this.state.currentColor];
              savedColors.unshift(colorCopy);
              // if saved colors is length 5, remove the last element
              if (savedColors.length > 5)
                  savedColors.pop();
              this.setState({ savedColors });
              this.updateSavedColors();
          });
          // Update the appearance of each color grid based on the queue 
          this.updateSavedColors();
          // Add event handler to each color button
          const savedButtons = document.querySelectorAll(".cp-saved-colors");
          savedButtons.forEach(button => {
              button.addEventListener("click", () => {
                  const btn = button;
                  if (btn.dataset.value) {
                      // has a color so return it 
                      const colorsAsArr = btn.dataset.value.split(",").map(Number);
                      this.setState({ currentColor: colorsAsArr });
                      this.updateColorDisplay();
                      this.updateSliders(colorsAsArr);
                  }
              });
          });
      }
      /** updateSliders: updates the sliders based on the current color */
      updateSliders(color) {
          if (this.isRGB) {
              this.sliders.forEach((slider, index) => {
                  slider.setValue(color[index]);
              });
          }
          else {
              const hsbColor = RGBAToHSBA(color[0], color[1], color[2], color[3]);
              this.HSBA = hsbColor;
              this.sliders.forEach((slider, index) => {
                  slider.setValue(hsbColor[index]);
              });
              this.updateSlideGradients(hsbColor[0], hsbColor[1], hsbColor[2]);
          }
      }
      /** updateSavedColors: updates the appearance of the saved colors based on the current state of the saved colors array */
      updateSavedColors() {
          const savedColors = this.state.savedColors;
          const savedSquares = document.querySelectorAll(".cp-saved-colors");
          // Reset all squares to default background
          savedSquares.forEach(square => {
              square.style.backgroundColor = '#f1f1f1';
              square.removeAttribute('data-value');
          });
          // Assign colors to squares
          for (let i = 0; i < savedColors.length; i++) {
              const squareIndex = savedSquares.length - 1 - i;
              if (savedSquares[squareIndex]) {
                  const square = savedSquares[squareIndex];
                  square.style.backgroundColor = arrToString(savedColors[i]);
                  square.setAttribute('data-value', savedColors[i].join(","));
              }
          }
      }
      /** createRGB: creates the RGB sliders */
      createRGB() {
          // Callback function for slider to change currentColor 
          const updateRGBColor = (colorIndex, sliderValue) => {
              const newColor = [...this.state.currentColor];
              newColor[colorIndex] = sliderValue;
              this.setState({ currentColor: newColor, colorType: "rgb" });
              this.updateColorDisplay();
          };
          const parent = document.querySelector('.cp-sliders');
          parent.innerHTML = '';
          this.sliders = [
              new Slider(parent, this.state.currentColor[0], 0, 255, 'Red', '200px', Localized('Red'), (value) => updateRGBColor(0, value), true),
              new Slider(parent, this.state.currentColor[1], 0, 255, 'Green', '200px', Localized('Green'), (value) => updateRGBColor(1, value), true),
              new Slider(parent, this.state.currentColor[2], 0, 255, 'Blue', '200px', Localized('Blue'), (value) => updateRGBColor(2, value), true)
          ];
      }
      /** updateSlideGradients: Updates the gradients for the slider background if it is saturation and brightness slider */
      updateSlideGradients(hue, saturation, brightness) {
          // Saturation: gradient from white to full color (hue)
          const saturationGradient = `linear-gradient(to right, hsl(${hue}, 0%, 100%), hsl(${hue}, 100%, 50%))`;
          // Brightness: gradient from black to the hue-saturation color
          const brightnessGradient = `linear-gradient(to right, #000, hsl(${hue}, ${saturation}%, 50%))`;
          const saturationThumbColor = `hsl(${hue}, ${saturation}%, ${100 - saturation / 2}%)`;
          const brightnessThumbColor = `hsl(${hue}, ${saturation}%, ${brightness}%)`;
          // Set the CSS custom properties
          document.documentElement.style.setProperty('--saturation-gradient', saturationGradient);
          document.documentElement.style.setProperty('--brightness-gradient', brightnessGradient);
          document.documentElement.style.setProperty('--saturation-thumb', saturationThumbColor);
          document.documentElement.style.setProperty('--brightness-thumb', brightnessThumbColor);
      }
      /** createHSB: creates the HSB sliders */
      createHSB() {
          const parent = document.querySelector('.cp-sliders');
          parent.innerHTML = '';
          const colorAsHSB = RGBAToHSBA(this.state.currentColor[0], this.state.currentColor[1], this.state.currentColor[2], this.state.currentColor[3]);
          this.HSBA = colorAsHSB;
          this.updateSlideGradients(this.HSBA[0], this.HSBA[1], this.HSBA[2]);
          // Callback function for slider to change HSB and update currentColor
          const updateHSBColor = (hsbIndex, sliderValue) => {
              this.HSBA[hsbIndex] = sliderValue;
              const newRGB = HSBAToRGBA(this.HSBA[0], this.HSBA[1], this.HSBA[2], this.HSBA[3]);
              this.setState({ currentColor: newRGB, colorType: "hsb" });
              this.updateColorDisplay();
              this.updateSlideGradients(this.HSBA[0], this.HSBA[1], this.HSBA[2]);
          };
          this.sliders = [
              new Slider(parent, colorAsHSB[0], 0, 360, 'Hue', '200px', Localized('Hue'), (value) => updateHSBColor(0, value), true),
              new Slider(parent, colorAsHSB[1], 0, 100, 'Saturation', '200px', Localized('Saturation'), (value) => updateHSBColor(1, value), true),
              new Slider(parent, colorAsHSB[2], 0, 100, 'Brightness', '200px', Localized('Brightness'), (value) => updateHSBColor(2, value), true)
          ];
      }
      /** init(): initializes the slider */
      init() {
          this.toDOM();
          this.updateColorDisplay();
          if (this.isRGB) {
              this.createRGB();
          }
          else {
              this.createHSB();
          }
          this.setupSavedColors();
      }
  }

  var img = "data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='%23a7a7a7' class='bi bi-caret-down-fill' viewBox='0 0 16 16'%3e %3cpath d='M7.247 11.14 2.451 5.658C1.885 5.013 2.345 4 3.204 4h9.592a1 1 0 0 1 .753 1.659l-4.796 5.48a1 1 0 0 1-1.506 0z'/%3e%3c/svg%3e";

  class ColorPicker {
      /** constructor: creates a Color Picker instance. A color picker has a parent div and an initial color */
      constructor(config, openTo = 'g') {
          // color display states that only ColorPicker needs to know about
          this.displayParameter = 'RGBA'; // true if the color display is in RGB mode, false if it is in HSLA mode
          this.isNetLogoNum = false; // true if the color display is in NetLogo number, false if its a compound number like Red + 2
          this.isMinimized = false; // default value for minimize is false
          this.copyMessageTimeout = null; //Keeps track of "Copied" message timeouts, so they don't stack and are cancelled if we switch colors 
          this.operatingMode = config.mode || 'DEFAULT';
          this.state = {
              currentColor: config.initColor,
              colorType: config.initColorType,
              currentMode: 'grid',
              changeModelColor: true,
              increment: 1,
              showNumbers: false,
              savedColors: config.savedColors || [],
          };
          const mode = config.mode || 'DEFAULT';
          if (mode !== 'DEFAULT' && mode !== 'RGBA' && mode !== 'NETLOGO') {
              throw new Error(`Invalid mode: ${mode}. Must be one of DEFAULT, RGBA, NETLOGO.`);
          }
          this.parent = config.parent;
          this.onColorSelect = config.onColorSelect;
          if (this.parent.offsetWidth < 600) {
              this.isMinimized = true;
          }
          this.openTo = openTo;
          this.init();
          console.log(this.operatingMode);
      }
      updateLayout() {
          const cpElement = this.parent.querySelector('.cp');
          if (cpElement) {
              if (this.isMinimized) {
                  cpElement.classList.add('cp-compact');
              }
              else {
                  cpElement.classList.remove('cp-compact');
              }
          }
      }
      /** setState: used to change the state of the color picker and call all update functions */
      setState(newState) {
          // Directly update properties of the existing state object
          Object.keys(newState).forEach(key => {
              this.state[key] = newState[key];
          });
          // Call update functions to reflect changes
          this.updateColorParameters();
          this.updateModelDisplay();
          // remove the copy timeout, if its there
          this.clearCopyTimeout();
      }
      /** init: initializes the ColorPicker */
      init() {
          this.toDOM();
          this.updateLayout();
          this.updateColorParameters();
          this.attachEventListeners();
          this.initAlphaSlider();
          this.updateAlphaBlockVisibility();
          this.updateValuesDisplayVisibility();
          // click the correct button to start 
          switch (this.openTo) {
              case 'wheel':
                  this.parent.querySelectorAll('.cp-mode-btn')[1].dispatchEvent(new Event('click'));
                  break;
              case 'rgb': // Updated from 'slider' to 'rgb'
                  this.parent.querySelectorAll('.cp-mode-btn')[2].dispatchEvent(new Event('click'));
                  break;
              case 'hsb': // New case for HSB mode
                  this.parent.querySelectorAll('.cp-mode-btn')[3].dispatchEvent(new Event('click'));
                  break;
              case 'sliderHSB':
                  this.parent.querySelectorAll('.cp-mode-btn')[2].dispatchEvent(new Event('click'));
                  // click the hsb button 
                  this.parent.querySelectorAll('.cp-slider-changer')[0].dispatchEvent(new Event('click'));
                  break;
              default:
                  this.parent.querySelectorAll('.cp-mode-btn')[0].dispatchEvent(new Event('click'));
          }
      }
      /** updateAlphaBlockVisibility: updates the visibility of the alpha block. Needed to hide or show the Alpha block based on the different operationModes */
      updateAlphaBlockVisibility() {
          if (this.operatingMode === 'NETLOGO') {
              const alphaContainer = this.parent.querySelector('.cp-alpha-cont');
              if (alphaContainer) {
                  alphaContainer.style.visibility = 'hidden';
                  alphaContainer.style.pointerEvents = 'none';
              }
          }
          // both NETLOGO and RGBA hide 'COLOR PARAMETERS'
          if (this.operatingMode === 'NETLOGO' || this.operatingMode === 'RGBA') {
              const parametersText = this.parent.querySelector('.cp-color-param-txt');
              if (parametersText) {
                  parametersText.style.visibility = 'hidden';
                  parametersText.style.pointerEvents = 'none';
              }
          }
      }
      /**
       * updateValuesDisplayVisibility:
       * Updates the visibility of color parameter displays based on the current operating mode.
       * - Hides NetLogo values when in 'RGBA' mode.
       * - Hides RGBA values when in 'NETLOGO' mode.
       * - Shows all values for other modes (e.g., 'DEFAULT').
       */
      updateValuesDisplayVisibility() {
          if (this.operatingMode == 'DEFAULT') {
              return; // return, because we don't need to hide anything 
          }
          // hide NetLogo display
          if (this.operatingMode == 'RGBA') {
              let netlogoDisplay = document.querySelector('#cp-values-display-netlogo');
              if (netlogoDisplay) {
                  netlogoDisplay.style.display = 'none';
              }
          }
          else {
              let rgbaDisplay = document.querySelector('#cp-values-display-other');
              if (rgbaDisplay) {
                  rgbaDisplay.style.display = 'none';
              }
          }
          // both methods add two buttons 'OKAY', 'CANCEL', my guess is you want to return the color both times? So destroy and call the callback function
          let colorDisplayCont = document.querySelector('.cp-values-display-cont');
          if (colorDisplayCont) {
              let buttonCont = document.createElement('div');
              buttonCont.style.display = 'flex';
              buttonCont.style.justifyContent = 'space-between';
              let okayButton = document.createElement('button');
              okayButton.textContent = Localized('Confirm');
              okayButton.classList.add('cp-values-display-btn');
              okayButton === null || okayButton === void 0 ? void 0 : okayButton.addEventListener('click', () => this.handleClose());
              let cancelButton = document.createElement('button');
              cancelButton.textContent = Localized('Cancel');
              cancelButton.classList.add('cp-values-display-btn');
              cancelButton === null || cancelButton === void 0 ? void 0 : cancelButton.addEventListener('click', () => this.handleClose());
              buttonCont.appendChild(okayButton);
              buttonCont.appendChild(cancelButton);
              colorDisplayCont.appendChild(buttonCont);
          }
      }
      /** updateColorParameters: updates the displayed color parameters to reflect the current Color. Also updates the alpha slider value because I don't know where else to put it  */
      updateColorParameters() {
          // update the color parameter type display
          const colorParamType = this.parent.querySelectorAll('.cp-values-type-text');
          colorParamType[0].innerHTML = this.displayParameter;
          colorParamType[1].innerHTML = 'NetLogo';
          let colorParamDisplay = this.parent.querySelectorAll('.cp-values-value');
          if (this.displayParameter == 'RGBA') {
              colorParamDisplay[0].innerHTML = `[${this.state.currentColor[0]}, ${this.state.currentColor[1]}, ${this.state.currentColor[2]}, ${this.state.currentColor[3]}]`;
          }
          else if (this.displayParameter == 'HEX') {
              // hex display
              colorParamDisplay[0].innerHTML = `${rgbaToHex(this.state.currentColor[0], this.state.currentColor[1], this.state.currentColor[2], this.state.currentColor[3])}`;
          }
          else {
              // HSLA display
              const hsba = RGBAToHSBA(this.state.currentColor[0], this.state.currentColor[1], this.state.currentColor[2], this.state.currentColor[3]);
              colorParamDisplay[0].innerHTML = `[${hsba[0]}, ${hsba[1]}, ${hsba[2]}, ${hsba[3]}]`;
          }
          // netlogo color parameter update
          if (this.isNetLogoNum) {
              // netlogo number
              colorParamDisplay[1].innerHTML = `${rgbToNetlogo([this.state.currentColor[0], this.state.currentColor[1], this.state.currentColor[2]])}`;
          }
          else {
              const compoundColor = `${netlogoToCompound(rgbToNetlogo([this.state.currentColor[0], this.state.currentColor[1], this.state.currentColor[2]]))}`;
              const formattedColor = compoundColor.charAt(0).toUpperCase() + compoundColor.slice(1);
              colorParamDisplay[1].innerHTML = formattedColor;
          }
          this.updateAlphaSlider();
      }
      /** updateAlphaSlider(): updates the appearance of the alpha slider to match the current alpha value */
      updateAlphaSlider() {
          const val = this.state.currentColor[3];
          const alphaSlider = this.parent.querySelector(".cp-alpha-slider");
          if (alphaSlider)
              alphaSlider.value = val.toString();
      }
      /** updateModelDisplay: updates the color of the model/background and the color widget next to the mode buttons  */
      updateModelDisplay() {
          var _a, _b;
          const colorString = arrToString(this.state.currentColor);
          if (this.state.changeModelColor) {
              (_a = this.parent.querySelector('.cp-model-preview')) === null || _a === void 0 ? void 0 : _a.setAttribute('fill', colorString);
          }
          else {
              (_b = this.parent.querySelector('.cp-model-background')) === null || _b === void 0 ? void 0 : _b.setAttribute('fill', colorString);
          }
          const modeColorDisplay = this.parent.querySelector('.cp-mode-color-display');
          if (modeColorDisplay)
              modeColorDisplay.style.backgroundColor = colorString;
      }
      /** handleClose: Handles the closure of the color picker and triggers the onColorSelect callback */
      handleClose() {
          // Return the selected color, saved colors, and color type
          if (this.operatingMode == 'DEFAULT') {
              const selectedColorObj = {
                  netlogo: rgbToNetlogo(this.state.currentColor),
                  rgba: this.state.currentColor,
                  colorType: this.state.colorType
              };
              // Invoke the callback function with selected color data
              this.onColorSelect([selectedColorObj, this.state.savedColors]);
          }
          else if (this.operatingMode == 'RGBA') {
              // return just the RGBA number, as a string 
              this.onColorSelect(`[${this.state.currentColor[0]}, ${this.state.currentColor[1]}, ${this.state.currentColor[2]}, ${this.state.currentColor[3]}]`);
          }
          else {
              // netlogo color 
              const compoundColor = `${netlogoToCompound(rgbToNetlogo([this.state.currentColor[0], this.state.currentColor[1], this.state.currentColor[2]]))}`;
              const formattedColor = compoundColor.charAt(0).toUpperCase() + compoundColor.slice(1);
              this.onColorSelect(formattedColor);
          }
      }
      /** attachEventListeners: Attaches the event listeners to the ColorPicker body */
      attachEventListeners() {
          var _a, _b;
          /** changeButtonColor: Helper function to toggle button color */
          function changeButtonColor(button, isPressed) {
              // Set styles based on isPressed
              button.style.backgroundColor = isPressed ? '#5A648D' : '#E5E5E5';
              button.style.color = isPressed ? 'white' : 'black';
              let image = button.querySelector('.cp-mode-btn-img');
              if (image) {
                  image.classList.toggle('cp-inverted', isPressed);
              }
          }
          // attach event listeners to the mode buttons 
          let modeButtons = this.parent.querySelectorAll('.cp-mode-btn');
          // Grid Button
          modeButtons[0].addEventListener('click', () => {
              this.setState({ currentMode: 'grid' });
              changeButtonColor(modeButtons[0], true);
              changeButtonColor(modeButtons[1], false);
              changeButtonColor(modeButtons[2], false);
              changeButtonColor(modeButtons[3], false);
              new GridMode(this.parent.querySelector('.cp-body-mode-main'), this.state, this.setState.bind(this));
          });
          // Wheel Button
          modeButtons[1].addEventListener('click', () => {
              this.setState({ currentMode: 'wheel' });
              changeButtonColor(modeButtons[1], true);
              changeButtonColor(modeButtons[0], false);
              changeButtonColor(modeButtons[2], false);
              changeButtonColor(modeButtons[3], false);
              new WheelMode(this.parent.querySelector('.cp-body-mode-main'), this.state, this.setState.bind(this));
          });
          modeButtons[2].addEventListener('click', () => {
              this.setState({ currentMode: 'rgb' });
              changeButtonColor(modeButtons[2], true);
              changeButtonColor(modeButtons[0], false);
              changeButtonColor(modeButtons[1], false);
              changeButtonColor(modeButtons[3], false);
              new SliderMode(this.parent.querySelector('.cp-body-mode-main'), this.state, this.setState.bind(this), this, 'rgb');
          });
          // HSB Button
          modeButtons[3].addEventListener('click', () => {
              this.setState({ currentMode: 'hsb' });
              changeButtonColor(modeButtons[3], true);
              changeButtonColor(modeButtons[0], false);
              changeButtonColor(modeButtons[1], false);
              changeButtonColor(modeButtons[2], false);
              new SliderMode(this.parent.querySelector('.cp-body-mode-main'), this.state, this.setState.bind(this), this, 'hsb');
          });
          // attach event listener to model indicator button
          let modelIndicatorButton = this.parent.querySelector('.cp-model-indicator');
          modelIndicatorButton === null || modelIndicatorButton === void 0 ? void 0 : modelIndicatorButton.addEventListener('click', () => {
              this.state.changeModelColor = !this.state.changeModelColor; // we don't want to call set state, because it updates the appearance as well 
              modelIndicatorButton.querySelector('.cp-mode-btn-text').innerHTML = this.state.changeModelColor ? Localized('Foreground Color') : Localized('Background Color');
              changeButtonColor(modelIndicatorButton, !this.state.changeModelColor);
          });
          //attach event listener to close button
          const closeButton = this.parent.querySelector('.cp-close');
          closeButton === null || closeButton === void 0 ? void 0 : closeButton.addEventListener('click', () => this.handleClose());
          // attach switch color display parameters event listeners 
          const paramSwitchBtns = this.parent.querySelectorAll('.cp-values-type');
          // this is the RGBA / HSLA param button
          (_a = paramSwitchBtns[0]) === null || _a === void 0 ? void 0 : _a.addEventListener('click', () => {
              if (this.displayParameter == 'RGBA') {
                  this.displayParameter = 'HEX';
              }
              else if (this.displayParameter == 'HEX') {
                  this.displayParameter = "HSBA";
              }
              else if (this.displayParameter == 'HSBA') {
                  this.displayParameter = 'RGBA';
              }
              this.updateColorParameters();
          });
          // NetLogo number --> doesn't have to switch text but does switch the display value 
          (_b = paramSwitchBtns[1]) === null || _b === void 0 ? void 0 : _b.addEventListener('click', () => {
              this.isNetLogoNum = !this.isNetLogoNum;
              this.updateColorParameters();
          });
          // add event listeners to copy elements 
          const valueDisplayElements = this.parent.querySelectorAll(".cp-values-value");
          valueDisplayElements.forEach((display, index) => {
              const displayAsElement = display;
              displayAsElement.addEventListener('click', () => {
                  this.copyToClipboard(displayAsElement);
              });
          });
      }
      /** clearCopyTimeout: Helper function to clear the copy timeout if necessary, and reset all values */
      clearCopyTimeout() {
          if (this.copyMessageTimeout) {
              clearTimeout(this.copyMessageTimeout);
              this.copyMessageTimeout = null;
              // also reset the state of all display elements
              const valueDisplayElements = document.querySelectorAll(".cp-values-value");
              valueDisplayElements.forEach((display, index) => {
                  const displayAsElement = display;
                  displayAsElement.style.pointerEvents = 'auto';
                  displayAsElement.style.opacity = '1';
              });
          }
      }
      /** Copies innerText of displayElement to the clipboard */
      copyToClipboard(displayElement) {
          const originalText = displayElement.innerText;
          const textToCopy = originalText;
          navigator.clipboard.writeText(textToCopy).then(() => {
              // Change to "Copied!"
              displayElement.innerText = 'Copied!';
              // Set a timeout to revert back to the original text
              // clear previous timeouts
              if (this.copyMessageTimeout) {
                  clearTimeout(this.copyMessageTimeout);
              }
              // don't let user copy this 
              displayElement.style.pointerEvents = 'none';
              displayElement.style.opacity = "0.5";
              this.copyMessageTimeout = window.setTimeout(() => {
                  displayElement.innerText = originalText;
                  this.copyMessageTimeout = null;
                  displayElement.style.opacity = "1";
                  displayElement.style.pointerEvents = 'auto';
              }, 1500);
          }).catch(err => {
              console.error('Failed to copy: ', err);
          });
      }
      /** initAlphaSlider: initializes the alpha slider */
      initAlphaSlider() {
          let alphaSlider = this.parent.querySelector('.cp-alpha-slider');
          alphaSlider.addEventListener('input', () => {
              this.setState({ currentColor: [this.state.currentColor[0], this.state.currentColor[1], this.state.currentColor[2], parseInt(alphaSlider.value)] });
              (this.state);
          });
      }
      /** toDOM: creates and attaches the ColorPicker body to parent */
      toDOM() {
          // localize strings before adding to dom
          const cpBody = `
        <div class="cp">
            <div class="cp-header">
                <div class="cp-title">
                    <img class="cp-icon" src="${img$6}">
                    <span class="cp-title-text"> ${Localized('Color Swatches')} </span>
                </div>
                <img class="cp-close" src="${img$5}"/>
            </div>

            <div class="cp-body">
                <div class="cp-body-left">
                    <div class="cp-mode-btn-cont">
                        <button class="cp-mode-btn">
                            <img class="cp-mode-btn-img" src="${img$4}"/>
                            <span class="cp-mode-btn-text">${Localized('Grid')}</span> 
                        </button>
                        <button class="cp-mode-btn">
                            <img class="cp-mode-btn-img" src="${img$3}"/>
                            <span class="cp-mode-btn-text">${Localized('Wheel')}</span> 
                        </button>
                        <button class="cp-mode-btn">
                            <img class="cp-mode-btn-img" src="${img$2}"/>
                            <span class="cp-mode-btn-text">RGB</span> 
                        </button>
                        <button class="cp-mode-btn"> <!-- New HSB button -->
                            <img class="cp-mode-btn-img" src="${img$2}"/>
                            <span class="cp-mode-btn-text">HSB</span> 
                        </button>
                    </div>
                    <div class="cp-body-mode-main no-select"></div>
                </div>
                <div class="cp-body-right">
                    <button class="cp-mode-btn cp-model-indicator"> 
                        <img class="cp-mode-btn-img" src="${img$1}"/>
                        <span class="cp-mode-btn-text">${Localized('Foreground Color')}</span>
                    </button>
                    <div class="cp-color-preview">
                        <svg viewBox="0 0 100 100">
                            <rect x="0" y="0" width="100" height="100" fill="white" class="cp-model-background"/>
                            <path xmlns="http://www.w3.org/2000/svg" class="cp-model-preview" fill="white" d="M 50.069 9.889 L 14.945 89.069 L 50.71 65.458 L 86.458 89.73 L 50.069 9.889 Z"/>
                        </svg>
                    </div>
                    <div class="cp-alpha-cont">
                        <span class="cp-alpha-text">${Localized('Alpha')}</span>
                        <input type="range" min="0" max="255" value="${this.state.currentColor[3]}" class="cp-styled-slider cp-alpha-slider color-alpha slider-progress">
                    </div>
                    <div class="cp-values-display-cont">
                        <span class="cp-color-param-txt">${Localized('Color Parameters')}</span>
                        <div id="cp-values-display-other" class="cp-values-display">
                            <div class="cp-values-type cp-values-type-1">
                                <div class="cp-values-cont">
                                    <span class="cp-values-type-text"></span>
                                    <img class="cp-values-img" src="${img}">
                                </div>
                            </div>
                            <div class="cp-values-value-cont cp-values-value-cont-1">
                                <span class="cp-values-value"></span>
                            </div>
                            
                        </div>
                        <div id="cp-values-display-netlogo" class="cp-values-display">
                            <div class="cp-values-type">
                                <div class="cp-values-cont">
                                    <span class="cp-values-type-text"></span>
                                    <img class="cp-values-img" src="${img}">
                                </div>
                            </div>
                            <div class="cp-values-value-cont">
                                <span class="cp-values-value"></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
          this.parent.innerHTML = cpBody;
      }
  }
  /** Localized: a helper function to get localized strings from the editor. */
  // If the EditorLocalized object is available, use it to get the localized string
  // Otherwise, return the source string
  function Localized(Source, ...Args) {
      var Localized = window.EditorLocalized;
      if (Localized) {
          return Localized.Get(Source, Args);
      }
      else {
          return Source;
      }
  }

  exports.ColorPicker = ColorPicker;
  exports.Localized = Localized;

}));
