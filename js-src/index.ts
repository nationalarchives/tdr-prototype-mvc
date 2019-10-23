import { updateFileStatuses } from "./fileStatus";
import { upload } from "./upload";

window.onload = function() {
  const uploadContainer: HTMLDivElement | null = document.querySelector(".upload-form");
  if (uploadContainer) {
    upload();
  }
  const fileStatusContainer: HTMLDivElement | null = document.querySelector(".file-status");
  if (fileStatusContainer) {
    updateFileStatuses();
  }
};
