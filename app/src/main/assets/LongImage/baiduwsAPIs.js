up = document.getElementById("up");
displayImg = document.getElementById("kantu");
imgSize = document.getElementById("daxiao");
heightAndWidth = document.getElementById("gaokuan");
up.onclick = function () {
    displayImg.innerHTML = "";
    imgSize.innerHTML = "";
    heightAndWidth.innerHTML = "";
    getValue();
};
up.ondrop = function () {
    up.value = "";
    displayImg.innerHTML = "";
    imgSize.innerHTML = "";
    heightAndWidth.innerHTML = "";
    getValue();
};
$(".option").change(function () {
    up.value = "";
});

function getValue() {
    var geshi = document.getElementById("geshi")["value"];
    hengshu = document.getElementById("hengshu")["value"];
    gk = document.getElementById("gk")["value"];
    zhengnixu = document.getElementById("zhengnixu")["value"];
    var quality = ~~document.getElementById("zhiliang")["value"];
    up.addEventListener("change", (chosenFiles) => {
        var imgList = Array.from(chosenFiles.target.files);
        processImgList(imgList, (imgs) => {
            CombineImgs(imgs, (imgSrcBase64) => {
                imgSize.innerHTML =
                    '<br><span style="color:black">大小：</span>' +
                    calculateSize(imgSrcBase64.length) +
                    "以上";
                displayHeightWidth(imgSrcBase64);
                displayImg.innerHTML =
                    '<div><img width="800" src=' + imgSrcBase64 + "></div>";
            });
        });
    });

    function displayHeightWidth(imgSrcBase64) {
        var image = new Image();
        image.src = imgSrcBase64;
        image.onload = function () {
            heightAndWidth.innerHTML =
                '<span style="color:black">高宽：</span>' +
                image.height +
                "*" +
                image.width +
                "（px）";
        };
    }

    function calculateSize(length) {
        var units = ["Bytes", "KB", "MB"];
        if (length == 0) {
            return "n/a";
        }
        var numericalValue = parseInt(
            Math.floor(Math.log(length) / Math.log(1024)),
        );
        return (
            (length / Math.pow(1024, numericalValue)).toFixed(1) +
            "\x20" +
            units[numericalValue]
        );
    }

    const processImgList = (files, processWholeImgList) => {
        var length = files.length;
        var imgList = [];
        var processedImgs = 0;
        files.forEach((file, index) => {
            var fileReader = new FileReader();
            fileReader.onload = () => {
                const image = new Image();
                image.src = fileReader.result;
                image.onload = () => {
                    imgList[index] = image;
                    processedImgs++;
                    if (processedImgs === length) {
                        if (zhengnixu == 1) {
                            processWholeImgList(imgList.reverse());
                        } else {
                            processWholeImgList(imgList);
                        }
                    }
                };
            };
            fileReader.readAsDataURL(file);
        });
    };

    function downloadCanvasImage(canvas, geshi) {
        var imageData;
        if (geshi == 1) {
            imageData = canvas.toDataURL("image/jpeg", quality);
        } else {
            imageData = canvas.toDataURL("image/png", quality);
        }
        Android.saveImage(imageData);
    }

    var CombineImgs = (images, imgBasicInfofunc) => {
        if (hengshu == 1) {
            var modifiedDimensions = images.map(
                (img) => (gk / img.width) * img.height,
            );
            var canvas = document.createElement("canvas");
            canvas.width = gk;
            canvas.height = modifiedDimensions.reduce((a, b) => a + b);
            var context = canvas.getContext("2d");
            var yPos = 0;
            images.forEach((img, index) => {
                var height = modifiedDimensions[index];
                context.drawImage(img, 0, yPos, gk, height);
                yPos += height;
            });
        } else {
            var modifiedDimensions = images.map(
                (img) => (gk / img.height) * img.width,
            );
            var canvas = document.createElement("canvas");
            canvas.width = modifiedDimensions.reduce((a, b) => a + b);
            canvas.height = gk;
            var context = canvas.getContext("2d");
            var xPos = 0;
            images.forEach((img, index) => {
                var width = modifiedDimensions[index];
                context.drawImage(img, xPos, 0, width, gk);
                xPos += width;
            });
        }
        if (geshi == 1) {
            imgBasicInfofunc(canvas.toDataURL("image/jpeg", quality));
        } else {
            imgBasicInfofunc(canvas.toDataURL("image/png", quality));
        }
        downloadCanvasImage(canvas, geshi);
    };
}
