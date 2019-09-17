package io.espinosa.hdfs;
/*
 * Copyright 2019 Allan Espinosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public interface ActualZkfc {
    // Copied from ZKFailOverController.ERR_CODE_FORMAT_DENIED
    public static int FORMAT_DENIED = 2;
    public static int FORMAT_SUCCESSFUL = 0;

    public int format() throws Exception;

    public int start() throws Exception;
}
