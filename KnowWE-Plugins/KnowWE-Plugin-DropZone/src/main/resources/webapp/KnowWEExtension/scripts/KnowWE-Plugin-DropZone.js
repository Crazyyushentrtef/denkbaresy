/*
 * Copyright (C) 2020 denkbares GmbH. All rights reserved.
 */

KNOWWE = typeof KNOWWE === "undefined" ? {} : KNOWWE;
KNOWWE.core = KNOWWE.core || {};
KNOWWE.core.plugin = KNOWWE.core.plugin || {};

/**
 * Namespace: KNOWWE.core.plugin.dropZone for debugging d3web expressions in KnowWE
 */
KNOWWE.core.plugin.dropZone = function() {

  function handleDragOver(event) {
    if (!isEventWithFiles(event)) return;
    event.stopPropagation();
    event.preventDefault();
    setDragOverStyle(event.target);
    const dropIndicator = jq$(event.target).closest('.dropzone').find(".drop-indicator").first();
    if (dropIndicator.length === 0) return;
    dropIndicator[0].addEventListener('dragleave', handleDragLeave);
    event.dataTransfer.dropEffect = 'copy';
  }


  function handleDragLeave(event) {
    resetStyle(event.target);
  }

  function prepareUploadData(event, pageName) {
    if (!isEventWithFiles(event)) return;
    event.stopPropagation();
    event.preventDefault();
    let form = jq$(event.target).closest('.dropzone').find('.drop-indicator').first();
    const ajaxData = new FormData();
    if (pageName == null) pageName = KNOWWE.helper.getPagename();
    ajaxData.append('page', pageName);
    ajaxData.append('nextpage', 'Upload.jsp?page=' + pageName);
    ajaxData.append('action', 'upload');

    let files = [];
    if (typeof event.dataTransfer !== "undefined") {
      files = event.dataTransfer.files; // Dropped files
    }
    const input = form.find('input[type="file"]').first();
    if (files.length === 0) {
      if (typeof input.context.files !== "undefined" && input.context.files.length > 0) {
        files.append(input.context.files); // Manually chosen files w/ file chooser
      } else {
        setClass(event.target, "uploading", "Not a valid attachment...");
        setTimeout(function() {
          resetStyle(event.target, "Drop attachment(s) here");
        }, 1000);
        return null;
      }
    }

    setUploadingStyle(event.target);
    Array.prototype.forEach.call(files, function(file) {
      ajaxData.append(input.attr('name'), file);
    });

    return {
      form: form,
      ajaxData: ajaxData
    };
  }

  function isEventWithFiles(event) {
    var temp = (event.originalEvent || event).dataTransfer;
    return temp && (temp = temp.types) && (temp.indexOf('Files') !== -1);
  }

  function ajaxData(uploadData, event) {
    uploadFile(uploadData.ajaxData, uploadData.form, function() {
      setUploadedStyle(event.target);
      if (!event.reload) {
        setTimeout(function() {
          resetStyle(event.target, "Drop attachment(s) here");
        }, 1000);
        if (event.callback) event.callback();
      } else {
        setTimeout(function() {
          window.location.reload();
        }, 1000);
      }
    }, function(data) {
      KNOWWE.notification.error(data.responseText);
      resetStyle(event.target, "Drop attachment(s) here");
    });
  }

  function uploadFile(data, form, successCallback, errorCallback) {
    jq$.ajax({
      url: form.attr("action"),
      type: form.attr("method"),
      data: data,
      cache: false,
      contentType: false,
      processData: false,
      success: successCallback,
      error: errorCallback
    });
  }

  function handleDropToExisting(event) {
    if (!isEventWithFiles(event)) return;
    event.reload = true;
    const uploadData = prepareUploadData(event);
    ajaxData(uploadData, event);
  }

  function setUploadingStyle(element) {
    setClass(element, "uploading", "Upload in progress...");
  }

  function setDragOverStyle(element) {
    setClass(element, "dragover", null);
  }

  function setUploadedStyle(element) {
    setClass(element, "uploaded", "Upload complete 🎉");
  }

  function resetStyle(element, title) {
    setClass(element, "", title);
  }

  function setClass(element, className, title) {
    const dropZone = jq$(element).closest('.dropzone');
    const boxInput = dropZone.find(".box-input").first();
    boxInput.attr('class', 'box-input');
    boxInput.addClass(className);
    const dropIndicator = dropZone.find('.drop-indicator').first();
    dropIndicator.attr('class', 'drop-indicator'); // Remove all other classes
    dropIndicator.addClass(className);
    const dropTextWrapper = dropIndicator.find("label").first();
    if (title !== null) dropTextWrapper.html(title);
    dropTextWrapper.removeClass();
    dropTextWrapper.addClass(className);
  }

  function attachDropZoneToElement(element, actionUrl, multiple, title, mode) {
    if (element == null || typeof element === "undefined" || element.length === 0) return;
    const form =
      '<form class="drop-indicator" method="post" action=' + actionUrl + ' enctype="multipart/form-data">' +
      '  <div class="box-input">' +
      '    <input style="display: none" class="box__file" type="file" name="files" id="file" ' +
      (multiple ? 'data-multiple-caption="{count} files selected" multiple' : '') + ' />' +
      '    <label for="file"><span class="box__dragndrop"/>' + title + '</span></label>' +
      '  </div>' +
      '</form>';

    if (mode === "full-height") {
      element.addClass("dropzone full-height");
      element.prepend(form);
    } else if (mode === "append") {
      element.append('<div class="dropzone">' + form + '</div>');
    } else if (mode === "replace") {
      element.addClass("dropzone replace");
      element.prepend(form);
    }
  }

  return {

    prepareUploadData: function(event, name) {
      return prepareUploadData(event, name);
    },

    uploadData: function(data, event) {
      ajaxData(data, event);
    },

    uploadFile: function(data, form, successCallback, errorCallback) {
      uploadFile(data, form, successCallback, errorCallback);
    },

    setDropZoneStyleUploading: function(element) {
      setUploadingStyle(element);
    },

    setDropZoneStyleUploaded: function(element) {
      setUploadedStyle(element);
    },

    resetDropZoneStyle: function(element, title) {
      resetStyle(element, title);
    },

    initAttachToExisting: function() {
      KNOWWE.core.plugin.dropZone.addDropZoneTo('div.page', "Drop attachment(s)", handleDropToExisting);
    },

    addDropZoneTo: function(elementSelector, title, dropHandlerCallback, actionUrl, mode, multiple) {
      if (!actionUrl) actionUrl = 'attach';
      if (!mode) mode = "full-height";
      if (typeof multiple === "undefined") multiple = true;
      const canWrite = jq$('#knowWEInfoCanWrite').attr('value');
      if (!KNOWWE.core.util.isHaddockTemplate() || typeof canWrite === 'undefined' || canWrite !== 'true') return;

      const elements = jq$(elementSelector);
      attachDropZoneToElement(elements, actionUrl, multiple, title, mode);

      elements.each(function() {
        this.addEventListener('dragover', handleDragOver);
        this.addEventListener('drop', dropHandlerCallback);
      });
      elements.find('input').first().on('change', dropHandlerCallback);
    }
  };

}
();
