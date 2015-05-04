var callbacks = [];

extension.setMessageListener(function (msg) {
                                 // each callback only gets invoked once; we delete it after
                                 // calling it
                                 for (var i = 0; i < callbacks.length; i += 1) {
                                     callbacks[i](msg);
                                     delete callbacks[i];
                                 };
                             });

exports.alive = function (callback) {
    callbacks.push(callback);
    extension.postMessage("alive");
};

exports.aliveSync = function () {
    return extension.internal.sendSyncMessage("alive");
};

exports.reboot = function (callback) {
    callbacks.push(callback);
    extension.postMessage("reboot");
};

exports.rebootSync = function () {
    return extension.internal.sendSyncMessage("reboot");
};

exports.shutdown = function (callback) {
    callbacks.push(callback);
    extension.postMessage("shutdown");
};

exports.shutdownSync = function () {
    return extension.internal.sendSyncMessage("shutdown");
};

exports.terminate = function (callback) {
    callbacks.push(callback);
    extension.postMessage("terminate");
};

exports.terminateSync = function () {
    return extension.internal.sendSyncMessage("terminate");
};

exports.relaunch = function (callback) {
    callbacks.push(callback);
    extension.postMessage("relaunch");
};

exports.relauchSync = function () {
    return extension.internal.sendSyncMessage("relaunch");
};

exports.read_system_config = function (callback) {
    callbacks.push(callback);
    extension.postMessage("read_sys_conf");
};

exports.read_system_configSync = function () {
    return extension.internal.sendSyncMessage("read_sys_config");
};

exports.read_cosmetic_config = function (callback) {
    callbacks.push(callback);
    extension.postMessage("read_cos_conf");
};

exports.read_cosmetic_configSync = function () {
    return extension.internal.sendSyncMessage("read_cos_config");
};

exports.write_system_config = function (callback, jsonstring) {
    callbacks.push(callback);
    extension.postMessage("write_sys_config");
    callbacks.push(callback);
    extension.postMessage(jsonstring);
};

exports.write_system_configSync = function (jsonstring) {
    extension.internal.sendSyncMessage("write_sys_config");
    extension.internal.sendSyncMessage(jsonstring);
};

exports.write_cosmetic_config = function (callback, jsonstring) {
    callbacks.push(callback);
    extension.postMessage("write_cos_config");
    callbacks.push(callback);
    extension.postMessage(jsonstring);
};

exports.write_cosmetic_configSync = function (jsonstring) {
    extension.internal.sendSyncMessage("write_cos_config");
    extension.internal.sendSyncMessage(jsonstring);
};

