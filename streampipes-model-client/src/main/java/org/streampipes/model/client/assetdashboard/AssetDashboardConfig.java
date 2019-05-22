/*
Copyright 2019 FZI Forschungszentrum Informatik

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.streampipes.model.client.assetdashboard;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AssetDashboardConfig {

  private @SerializedName("_id") String dashboardId;
  private @SerializedName("_rev") String rev;

  private String dashboardName;
  private String dashboardDescription;
  private ImageInfo imageInfo;
  private CanvasAttributes attrs;
  private String className;
  private List<CanvasElement> children;

  public AssetDashboardConfig() {
  }

  public String getDashboardName() {
    return dashboardName;
  }

  public void setDashboardName(String dashboardName) {
    this.dashboardName = dashboardName;
  }

  public String getDashboardDescription() {
    return dashboardDescription;
  }

  public void setDashboardDescription(String dashboardDescription) {
    this.dashboardDescription = dashboardDescription;
  }

  public CanvasAttributes getAttrs() {
    return attrs;
  }

  public void setAttrs(CanvasAttributes attrs) {
    this.attrs = attrs;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public List<CanvasElement> getChildren() {
    return children;
  }

  public void setChildren(List<CanvasElement> children) {
    this.children = children;
  }

  public ImageInfo getImageInfo() {
    return imageInfo;
  }

  public void setImageInfo(ImageInfo imageInfo) {
    this.imageInfo = imageInfo;
  }

  public String getDashboardId() {
    return dashboardId;
  }

  public void setDashboardId(String dashboardId) {
    this.dashboardId = dashboardId;
  }

  public String getRev() {
    return rev;
  }

  public void setRev(String rev) {
    this.rev = rev;
  }
}
