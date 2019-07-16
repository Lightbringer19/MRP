if (package.isFinished()) {
    var dir = "C:/Users/mhnyc/Desktop/!!FULL AUTO/Files/SCHEDULE/";
    var downloadName = package.getName(); // Name
    var packagePath = package.getDownloadFolder() + "\\";
    var fileName = downloadName + ".json";

    if (!getPath(dir).exists()) getPath(dir).mkdirs();
    +
        writeFile(dir + fileName, packagePath, true);
}